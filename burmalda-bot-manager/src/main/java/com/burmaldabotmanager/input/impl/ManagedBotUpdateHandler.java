package com.burmaldabotmanager.input.impl;

import com.burmaldabotmanager.input.TelegramInputHandlerBase;
import com.burmaldabotmanager.service.ManagedBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ManagedBotUpdateHandler implements TelegramInputHandlerBase {
    private final ManagedBotService managedBotService;

    @Override
    public boolean supports(Update update) {
        Message message = update.getMessage();
        return message != null && update.hasMessage() && message.getManagedBotCreated() != null;
    }

    @Override
    public Optional<SendMessage> handle(Update update) {
        managedBotService.registerManagedBot(update.getMessage().getManagedBotCreated().getBot());
        return Optional.empty();
    }
}
