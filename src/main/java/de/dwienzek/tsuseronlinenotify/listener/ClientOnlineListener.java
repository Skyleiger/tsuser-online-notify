package de.dwienzek.tsuseronlinenotify.listener;

import com.github.manevolent.ts3j.event.ClientJoinEvent;
import com.github.manevolent.ts3j.event.ClientLeaveEvent;
import com.github.manevolent.ts3j.event.TS3Listener;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import de.dwienzek.tsuseronlinenotify.TSUserOnlineNotify;
import de.dwienzek.tsuseronlinenotify.util.OnlineUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientOnlineListener implements TS3Listener {

    private static final Logger LOGGER = LogManager.getLogger(ClientOnlineListener.class);

    @Override
    public void onClientJoin(ClientJoinEvent event) {
        if (
                TSUserOnlineNotify.getInstance().getConfiguration().getStringList("notifyUsers")
                        .stream()
                        .anyMatch(notifyUser -> event.getClientNickname().equalsIgnoreCase(notifyUser) || event.getUniqueClientIdentifier().equalsIgnoreCase(notifyUser))
        ) {
            TSUserOnlineNotify.getInstance().getOnlineUsers().add(new OnlineUser(event.getUniqueClientIdentifier(), event.getClientId(), event.getClientNickname()));
            LOGGER.info("The user [clientUniqueId={}, clientId={}, nickname={}] is now online.", event.getUniqueClientIdentifier(), event.getClientId(), event.getClientNickname());
            TSUserOnlineNotify.getInstance().getTelegramBot().execute(new SendMessage(TSUserOnlineNotify.getInstance().getConfiguration().getLong("telegram.chatId"), "Der Nutzer " + event.getClientNickname() + " ist nun auf dem TS."), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                    LOGGER.info("Message to telegram sent.");
                }

                @Override
                public void onFailure(SendMessage sendMessage, IOException exception) {
                    LOGGER.error("Failed to send online message to telegram", exception);
                }
            });
        }
    }

    @Override
    public void onClientLeave(ClientLeaveEvent event) {
        TSUserOnlineNotify.getInstance().getOnlineUsers()
                .stream()
                .filter(onlineUser -> event.getClientId() == onlineUser.getClientId())
                .findFirst().ifPresent(onlineUser -> {
            LOGGER.info("The user [clientUniqueId={}, clientId={}, nickname={}] is now offline.", onlineUser.getClientUniqueId(), onlineUser.getClientId(), onlineUser.getNickname());
            TSUserOnlineNotify.getInstance().getTelegramBot().execute(new SendMessage(TSUserOnlineNotify.getInstance().getConfiguration().getLong("telegram.chatId"), "Der Nutzer " + onlineUser.getNickname() + " hat den TS verlassen."), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                    LOGGER.info("Message to telegram sent.");
                }

                @Override
                public void onFailure(SendMessage sendMessage, IOException exception) {
                    LOGGER.error("Failed to send online message to telegram", exception);
                }
            });
        });
    }

}
