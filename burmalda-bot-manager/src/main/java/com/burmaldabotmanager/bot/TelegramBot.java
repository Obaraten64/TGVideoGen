package com.burmaldabotmanager.bot;

import com.burmaldabotmanager.input.TelegramInputDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
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
        Optional<SendMessage> optionalResponse = telegramInputDispatcher.dispatch(update);
        if (optionalResponse.isEmpty()) {
            return;
        }

        telegramSender.sendMessage(optionalResponse.get(), chat_id);
    }
}
