package com.burmaldabot.input;

import com.burmaldabot.model.bot.BotContext;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

public interface TelegramInputHandlerBase {
    boolean supports(Message message);

    Optional<String> handle(Message message, BotContext context);
}
