package de.dwienzek.tsuseronlinenotify.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
public class MessageConfiguration {

    String userOnlineMessage;
    String userOfflineMessage;

    public MessageConfiguration(@Value("${message.user.online}") String userOnlineMessage,
                                @Value("${message.user.offline}") String userOfflineMessage) {
        this.userOnlineMessage = userOnlineMessage;
        this.userOfflineMessage = userOfflineMessage;
    }

}
