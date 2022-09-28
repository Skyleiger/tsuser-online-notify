package de.dwienzek.tsuseronlinenotify.component.ts3listener;

import com.github.manevolent.ts3j.event.ClientJoinEvent;
import com.github.manevolent.ts3j.event.ClientLeaveEvent;
import com.github.manevolent.ts3j.event.TS3Listener;
import de.dwienzek.tsuseronlinenotify.component.configuration.MessageConfiguration;
import de.dwienzek.tsuseronlinenotify.component.configuration.OnlineNotifyConfiguration;
import de.dwienzek.tsuseronlinenotify.dto.OnlineTS3User;
import de.dwienzek.tsuseronlinenotify.repository.OnlineTS3UserRepository;
import de.dwienzek.tsuseronlinenotify.service.TelegramService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ClientOnlineTS3Listener implements TS3Listener {

    private static final Logger LOGGER = LogManager.getLogger(ClientOnlineTS3Listener.class);

    private final OnlineNotifyConfiguration onlineNotifyConfiguration;
    private final MessageConfiguration messageConfiguration;
    private final OnlineTS3UserRepository onlineTS3UserRepository;
    private final TelegramService telegramService;

    @Override
    public void onClientJoin(ClientJoinEvent event) {
        if (isUserForNotify(event.getClientNickname(), event.getUniqueClientIdentifier())) {
            onlineTS3UserRepository.addOnlineTS3User(new OnlineTS3User(event.getClientId(), event.getUniqueClientIdentifier(), event.getClientNickname()));
            telegramService.sendMessage(messageConfiguration.getUserOnlineMessage().replaceAll("%nickname%", event.getClientNickname()));

            LOGGER.info("The user [clientId={}, clientUniqueId={}, nickname={}] is now online.", event.getClientId(), event.getUniqueClientIdentifier(), event.getClientNickname());
        }
    }

    @Override
    public void onClientLeave(ClientLeaveEvent event) {
        OnlineTS3User user = onlineTS3UserRepository.getOnlineTS3User(event.getClientId());

        if (user != null) {
            onlineTS3UserRepository.removeOnlineTS3User(user);
            telegramService.sendMessage(messageConfiguration.getUserOfflineMessage().replaceAll("%nickname%", user.getNickname()));

            LOGGER.info("The user [clientId={}, clientUniqueId={}, nickname={}] is now offline.", user.getClientId(), user.getClientUniqueId(), user.getNickname());
        }
    }

    private boolean isUserForNotify(String nickname, String identifier) {
        return onlineNotifyConfiguration.getUsers()
                .stream()
                .anyMatch(userForNotify -> nickname.equalsIgnoreCase(userForNotify) || identifier.equalsIgnoreCase(userForNotify));
    }

}
