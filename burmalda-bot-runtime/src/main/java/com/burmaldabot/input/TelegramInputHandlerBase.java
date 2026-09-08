package com.burmaldabot.input;

import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

public interface TelegramInputHandlerBase {
    boolean supports(Message message);

    Optional<String> handle(Message message);
}
