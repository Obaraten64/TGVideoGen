package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputDurationCallbackHandler implements CallbackQueryHandler {

    private final InputDurationStepHandler inputDurationStepHandler;
    private final FlowResponseService responseService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("gen:dur:");
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.INPUTTING_DURATION);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        int seconds = Integer.parseInt(data.substring("gen:dur:".length()));

        if (callbackQuery.getMessage() != null) {
            responseService.clearInlineMarkup(
                    context,
                    callbackQuery.getMessage().getMessageId(),
                    String.format("✅ Выбрано: **%d сек**", seconds)
            );
        }

        inputDurationStepHandler.processDuration(context, session, seconds);
    }
}
