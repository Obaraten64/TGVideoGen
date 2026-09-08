package com.burmaldabotmanager.command.impl;

import com.burmaldabotmanager.command.TelegramCommand;
import com.burmaldabotmanager.service.BotUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartCommand implements TelegramCommand {
    private final BotUrlService botUrlService;

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public SendMessage message() {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Create your own bot!")
                .url(botUrlService.prepareBotCreationUrl())
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();

        return SendMessage.builder()
                .chatId("temp")
                .text("Your own bot!")
                .replyMarkup(keyboard)
                .build();
    }

    @Override
    public String description() {
        return "Start command";
    }
}
