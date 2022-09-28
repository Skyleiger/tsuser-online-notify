package de.dwienzek.tsuseronlinenotify.component.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@lombok.Value
public class TelegramConfiguration {

    String token;
    long chatId;

    public TelegramConfiguration(@Value("${telegram.token}") String token,
                                 @Value("${telegram.chatId}") long chatId) {
        this.token = token;
        this.chatId = chatId;
    }

}
