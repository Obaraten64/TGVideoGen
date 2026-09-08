package com.burmaldabot.service.webhook;

import com.burmaldabot.input.TelegramInputDispatcher;
import com.burmaldabot.telegram.TelegramSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TelegramHandlerService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramInputDispatcher telegramInputDispatcher;
    private final TelegramSender telegramSender;
    private final String telegramToken;

    @Override
    public String getBotToken() {
        return telegramToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        Message message = update.getMessage();
        if (message == null || !update.hasMessage()) {
            return;
        }

        long chat_id = message.getChatId();
        Optional<String> optionalResponse = telegramInputDispatcher.dispatch(message);
        if (optionalResponse.isEmpty() || optionalResponse.get().isEmpty()) {
            return;
        }

        String responseMessage = optionalResponse.get();
        telegramSender.sendMessage(responseMessage, chat_id);
    }
}
