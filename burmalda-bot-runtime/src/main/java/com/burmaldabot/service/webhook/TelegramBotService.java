package com.burmaldabot.service.webhook;

import com.burmaldabot.model.bot.Bot;
import com.burmaldabot.model.bot.BotStatus;
import com.burmaldabot.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramBotService {

    private final BotRepository botRepository;

    public Bot getActiveBot(Long botId) {
        return botRepository.findById(botId)
                .filter(bot -> bot.getStatus() == BotStatus.ACTIVE)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Active bot not found: " + botId
                        )
                );
    }
}
