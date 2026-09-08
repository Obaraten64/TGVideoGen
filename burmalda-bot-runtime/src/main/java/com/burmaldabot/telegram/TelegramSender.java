package com.burmaldabot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramSender {
    private final TelegramClient telegramClient;

    public void sendMessage(String messageText, long chatId) {
        SendMessage messageToSend = SendMessage
                .builder()
                .chatId(chatId)
                .text(messageText)
                .build();
        try {
            telegramClient.execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendDocument(long chatId, byte[] file, String fileName) {
        SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(
                        new InputFile(
                                new ByteArrayInputStream(file),
                                fileName
                        )
                )
                .build();

        try {
            telegramClient.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}
