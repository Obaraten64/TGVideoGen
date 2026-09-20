package com.burmaldabot.input;

import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramInputDispatcher {
    private final List<TelegramInputHandlerBase> handlers;

    public Optional<String> dispatch(Message message, BotContext context) {
        return handlers.stream()
                .filter(handler -> handler.supports(message))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported Telegram input"))
                .handle(message, context);
    }
}
