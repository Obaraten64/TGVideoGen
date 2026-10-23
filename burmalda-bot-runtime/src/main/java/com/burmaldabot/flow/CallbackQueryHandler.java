package com.burmaldabot.flow;

import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler {
    boolean supports(String callbackData);
    void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery);
}
