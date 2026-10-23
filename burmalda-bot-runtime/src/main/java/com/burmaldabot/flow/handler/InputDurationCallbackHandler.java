package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputDurationCallbackHandler implements CallbackQueryHandler {

    private final InputDurationStepHandler inputDurationStepHandler;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("gen:dur:");
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        int seconds = Integer.parseInt(data.substring("gen:dur:".length()));
        inputDurationStepHandler.processDuration(context, session, seconds);
    }
}
