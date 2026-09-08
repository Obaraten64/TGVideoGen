package com.burmaldabot.config;

import com.burmaldabot.command.TelegramCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class TelegramConfiguration {
    @Value("${telegram.token}")
    private final String telegramToken;

    @Bean("telegramToken")
    public String telegramBotToken() {
        return telegramToken;
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramToken);
    }

    @Bean
    public Map<String, TelegramCommand> telegramCommands(List<TelegramCommand> telegramCommands) {
        return telegramCommands.stream()
                .collect(Collectors.toMap(TelegramCommand::command, Function.identity()));
    }
}
