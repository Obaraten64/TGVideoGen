package com.burmaldabot.service.webhook;

import com.burmaldabot.model.bot.Bot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramUpdateRouterService {
    private final TelegramBotService botService;
    private final TelegramHandlerService telegramHandlerService;

    public void route(Long botId, Update update) {
        Bot bot = botService.getActiveBot(botId);
        log.info("Update received for @{}", bot.getUsername());

        // TODO: update to add bot variable
        telegramHandlerService.consume(update);
    }
}
