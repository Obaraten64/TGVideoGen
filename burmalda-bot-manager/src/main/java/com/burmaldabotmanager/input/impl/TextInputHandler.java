package com.burmaldabotmanager.input.impl;

import com.burmaldabotmanager.input.TelegramInputHandlerBase;
import com.burmaldabotmanager.service.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TextInputHandler implements TelegramInputHandlerBase {
    private final CommandService commandService;

    @Override
    public boolean supports(Update update) {
        Message message = update.getMessage();
        return message != null && update.hasMessage() && message.hasText();
    }

    @Override
    public Optional<SendMessage> handle(Update update) {
        return commandService.handleMessage(update.getMessage().getText());
    }
}
