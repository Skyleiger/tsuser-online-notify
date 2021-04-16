package de.dwienzek.tsuseronlinenotify.listener;

import com.github.manevolent.ts3j.api.Client;
import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.event.ClientUpdatedEvent;
import com.github.manevolent.ts3j.event.TS3Listener;
import de.dwienzek.tsuseronlinenotify.TSUserOnlineNotify;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ClientChangedListener implements TS3Listener {

    private static final Logger LOGGER = LogManager.getLogger(ClientChangedListener.class);

    @Override
    public void onClientChanged(ClientUpdatedEvent event) {
        TSUserOnlineNotify.getInstance().getOnlineUsers()
                .stream()
                .filter(onlineUser -> event.getClientId() == onlineUser.getClientId())
                .findFirst().ifPresent(onlineUser -> {

            try {
                Client client = TSUserOnlineNotify.getInstance().getTeamspeakClient().getClientInfo(event.getClientId());

                if (!client.getNickname().equals(onlineUser.getNickname())) {
                    LOGGER.info("The user [clientUniqueId={}, clientId={}, nickname={}] changed his name to {}.", onlineUser.getClientUniqueId(), onlineUser.getClientId(), onlineUser.getNickname(), client.getNickname());
                    onlineUser.setNickname(client.getNickname());
                }
            } catch (IOException | TimeoutException | InterruptedException | CommandException exception) {
                LOGGER.error(new ParameterizedMessage("Failed to retrieve client info of client {}", event.getClientId()), exception);
            }

        });

    }

}
