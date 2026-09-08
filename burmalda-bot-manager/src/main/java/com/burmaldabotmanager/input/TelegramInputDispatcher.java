package com.burmaldabotmanager.input;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramInputDispatcher {
    private final List<TelegramInputHandlerBase> handlers;

    public Optional<SendMessage> dispatch(Update update) {
        return handlers.stream()
                .filter(handler -> handler.supports(update))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported Telegram input"))
                .handle(update);
    }
}
