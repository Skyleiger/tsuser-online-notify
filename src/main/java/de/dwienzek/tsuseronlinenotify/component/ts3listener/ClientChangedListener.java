package de.dwienzek.tsuseronlinenotify.component.ts3listener;

import com.github.manevolent.ts3j.command.CommandException;
import com.github.manevolent.ts3j.event.ClientUpdatedEvent;
import com.github.manevolent.ts3j.event.TS3Listener;
import de.dwienzek.tsuseronlinenotify.dto.OnlineTS3User;
import de.dwienzek.tsuseronlinenotify.repository.OnlineTS3UserRepository;
import de.dwienzek.tsuseronlinenotify.service.TeamSpeakService;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class ClientChangedListener implements TS3Listener {

    private static final Logger LOGGER = LogManager.getLogger(ClientChangedListener.class);

    private final TeamSpeakService teamSpeakService;
    private final OnlineTS3UserRepository onlineTS3UserRepository;

    public ClientChangedListener(@Lazy TeamSpeakService teamSpeakService, OnlineTS3UserRepository onlineTS3UserRepository) {
        this.teamSpeakService = teamSpeakService;
        this.onlineTS3UserRepository = onlineTS3UserRepository;
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void onClientChanged(ClientUpdatedEvent event) {
        OnlineTS3User user = onlineTS3UserRepository.getOnlineTS3User(event.getClientId());

        if (user != null) {
            try {
                String nickname = teamSpeakService.fetchNicknameOfClient(event.getClientId());

                if (!user.getNickname().equals(nickname)) {
                    LOGGER.info("The user [clientId={}, clientUniqueId={}, nickname={}] changed his name to '{}'.", user.getClientId(), user.getClientUniqueId(), user.getNickname(), nickname);
                    user.setNickname(nickname);
                }
            } catch (IOException | TimeoutException | CommandException exception) {
                LOGGER.error(new ParameterizedMessage("Failed to retrieve nickname of client '{}'.", event.getClientId()), exception);
            }
        }
    }

}
