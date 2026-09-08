package com.burmaldabotmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.managed.GetManagedBotToken;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
public class BotDataService {
    private final TelegramClient telegramClient;

    public String getManagedBotToken(Long botId) throws TelegramApiException {
        GetManagedBotToken request =
                new GetManagedBotToken(botId);
        return telegramClient.execute(request);
    }
}
