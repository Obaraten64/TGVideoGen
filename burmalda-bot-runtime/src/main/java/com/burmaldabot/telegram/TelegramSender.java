package com.burmaldabot.telegram;

import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
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
    public void sendMessage(String messageText, BotContext context) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        SendMessage messageToSend = SendMessage
                .builder()
                .chatId(context.chatId())
                .text(messageText)
                .build();
        try {
            telegramClient.execute(messageToSend);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendDocument(BotContext context, byte[] file, String fileName) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        SendDocument sendDocument = SendDocument.builder()
                .chatId(context.chatId())
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
