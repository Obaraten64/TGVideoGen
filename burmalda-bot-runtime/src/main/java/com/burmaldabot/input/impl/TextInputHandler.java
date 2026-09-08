package com.burmaldabot.input.impl;

import com.burmaldabot.input.TelegramInputHandlerBase;
import com.burmaldabot.service.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TextInputHandler implements TelegramInputHandlerBase {
    private final CommandService commandService;

    @Override
    public boolean supports(Message message) {
        return message.hasText();
    }

    @Override
    public Optional<String> handle(Message message) {
        return commandService.handleMessage(message.getText());
    }
}
