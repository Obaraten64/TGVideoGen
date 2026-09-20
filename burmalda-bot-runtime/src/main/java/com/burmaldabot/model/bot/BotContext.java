package com.burmaldabot.model.bot;

public record BotContext(
        Bot bot,
        Long chatId,
        Long telegramUserId
) {
}
