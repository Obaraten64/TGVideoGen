package com.burmaldabotmanager.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramSender {
    private final TelegramClient telegramClient;

    public void sendMessage(SendMessage message, long chatId) {
        message.setChatId(chatId);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
