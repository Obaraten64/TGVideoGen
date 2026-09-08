package com.burmaldabotmanager.input;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

public interface TelegramInputHandlerBase {
    boolean supports(Update update);

    Optional<SendMessage> handle(Update update);
}
