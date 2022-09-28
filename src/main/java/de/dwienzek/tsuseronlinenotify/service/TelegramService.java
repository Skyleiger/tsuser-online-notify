package de.dwienzek.tsuseronlinenotify.service;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import de.dwienzek.tsuseronlinenotify.component.configuration.TelegramConfiguration;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class TelegramService {

    private static final Logger LOGGER = LogManager.getLogger(TelegramService.class);

    private final TelegramConfiguration configuration;
    private TelegramBot bot;

    @Autowired
    public TelegramService(TelegramConfiguration configuration) {
        this.configuration = configuration;
        this.bot = new TelegramBot(configuration.getToken());
        bot.removeGetUpdatesListener();
    }

    public void sendMessage(String message) {
        LOGGER.debug("Sending message to telegram: '{}'", message);

        bot.execute(new SendMessage(configuration.getChatId(), message), new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                LOGGER.debug("Message to telegram sent: '{}'", message);
            }

            @Override
            public void onFailure(SendMessage sendMessage, IOException exception) {
                LOGGER.warn(new ParameterizedMessage("Message could not be sent to telegram: '{}'", message), exception);
            }
        });
    }

}
