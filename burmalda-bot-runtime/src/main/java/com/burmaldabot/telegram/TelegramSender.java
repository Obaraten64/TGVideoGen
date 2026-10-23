package com.burmaldabot.telegram;

import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramSender {

    public Message sendMessage(String messageText, BotContext context) {
        return sendMessage(context, messageText, null);
    }

    public Message sendMessage(BotContext context, String messageText, ReplyKeyboard replyKeyboard) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                .chatId(context.chatId())
                .text(messageText);

        if (replyKeyboard != null) {
            builder.replyMarkup(replyKeyboard);
        }

        try {
            return telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage(), e);
            return null;
        }
    }

    public void editMessageText(BotContext context, Integer messageId, String newText, InlineKeyboardMarkup keyboard) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        EditMessageText.EditMessageTextBuilder<?, ?> builder = EditMessageText.builder()
                .chatId(context.chatId())
                .messageId(messageId)
                .text(newText);

        if (keyboard != null) {
            builder.replyMarkup(keyboard);
        }

        try {
            telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            log.error("Failed to edit message {}: {}", messageId, e.getMessage(), e);
        }
    }

    public void answerCallbackQuery(BotContext context, String callbackQueryId, String text) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        AnswerCallbackQuery.AnswerCallbackQueryBuilder<?, ?> builder = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId);

        if (text != null && !text.isBlank()) {
            builder.text(text).showAlert(false);
        }

        try {
            telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query {}: {}", callbackQueryId, e.getMessage(), e);
        }
    }

    public void sendDocument(BotContext context, byte[] file, String fileName) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        SendDocument sendDocument = SendDocument.builder()
                .chatId(context.chatId())
                .document(new InputFile(new ByteArrayInputStream(file), fileName))
                .build();

        try {
            telegramClient.execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Failed to send document: {}", e.getMessage(), e);
        }
    }

    public void sendVideo(BotContext context, byte[] videoBytes, String fileName, String caption) {
        TelegramClient telegramClient = new OkHttpTelegramClient(context.bot().getToken());
        SendVideo.SendVideoBuilder<?, ?> builder = SendVideo.builder()
                .chatId(context.chatId())
                .video(new InputFile(new ByteArrayInputStream(videoBytes), fileName));

        if (caption != null && !caption.isBlank()) {
            builder.caption(caption);
        }

        try {
            telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            log.error("Failed to send video: {}", e.getMessage(), e);
        }
    }
}
