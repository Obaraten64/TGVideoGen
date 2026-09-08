package com.burmaldabotmanager.service;

import com.burmaldabotmanager.model.bot.Bot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    @Value("${telegram.managed.webhook}")
    private final String baseUrl;

    public void registerWebhook(Bot bot) throws TelegramApiException {
        String webhookUrl = baseUrl + bot.getId();
        SetWebhook request = SetWebhook.builder()
                .url(webhookUrl)
                .build();

        TelegramClient botClient =
                new OkHttpTelegramClient(bot.getToken());
        botClient.execute(request);

        log.info(
                "Webhook registered for bot {}: {}",
                bot.getId(),
                webhookUrl
        );
    }
}
