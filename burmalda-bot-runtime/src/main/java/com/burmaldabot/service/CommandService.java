package com.burmaldabot.service;

import com.burmaldabot.command.TelegramCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandService {
    @Qualifier("defaultCommand")
    private final TelegramCommand defaultCommand;
    private final Map<String, TelegramCommand> telegramCommands;

    public Optional<String> handleMessage(String message) {
        log.info("Handling command: {}", message);
        if (message != null && message.startsWith("/")) {
            String[] args = message.split(" ");
            return Optional.ofNullable(telegramCommands.getOrDefault(args[0], defaultCommand).message());
        }

        return Optional.empty();
    }
}
