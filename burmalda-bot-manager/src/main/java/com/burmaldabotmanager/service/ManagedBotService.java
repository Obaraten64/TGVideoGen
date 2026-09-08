package com.burmaldabotmanager.service;

import com.burmaldabotmanager.model.bot.Bot;
import com.burmaldabotmanager.model.bot.BotStatus;
import com.burmaldabotmanager.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagedBotService {
    private final BotRepository botRepository;
    private final BotDataService botDataService;
    private final WebhookService webhookService;

    @Transactional
    public Optional<Bot> registerManagedBot(User managedBot) {
        Long telegramBotId = managedBot.getId();
        String username = managedBot.getUserName();

        log.info("Registering managed bot: telegramBotId={}, username={}",
                telegramBotId, username);

        Bot bot = new Bot();
        bot.setTelegramBotId(telegramBotId);
        bot.setUsername(username);
        bot.setStatus(BotStatus.CREATING);

        try {
            // 1. Get token
            String token = botDataService
                    .getManagedBotToken(telegramBotId);

            // 2. Save bot
            bot.setToken(token);
            bot = botRepository.save(bot);

            // 3. Register webhook
            webhookService.registerWebhook(bot);

            bot.setStatus(BotStatus.ACTIVE);
            return Optional.of(botRepository.save(bot));
        } catch (TelegramApiException e) {
            log.error("Error registering managed bot via Telegram", e);
            bot.setStatus(BotStatus.FAILED);
            return Optional.of(botRepository.save(bot));
        }
    }
}
