package com.burmaldabot.flow;

import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Set;

public interface CallbackQueryHandler {

    boolean supports(String callbackData);

    /**
     * Allowed states from which this callback action can be executed.
     * An empty set signifies the callback is allowed from any state.
     */
    Set<UserFlowState> getAllowedStates();

    void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery);
}
