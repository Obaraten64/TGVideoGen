package com.burmaldabot.flow;

import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface UserFlowStepHandler {
    UserFlowState getSupportedState();
    void handleMessage(BotContext context, BotUserSession session, Message message);
}
