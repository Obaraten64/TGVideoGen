package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelFlowCallbackHandler implements CallbackQueryHandler {

    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    @Override
    public boolean supports(String callbackData) {
        return "gen:cancel".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(
                UserFlowState.SELECTING_RESOLUTION,
                UserFlowState.INPUTTING_DURATION,
                UserFlowState.COLLECTING_PHOTOS,
                UserFlowState.INPUTTING_PROMPT
        );
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        session.resetToMenu();
        sessionService.save(session);

        // Clear inline markup from previous message if available
        if (callbackQuery.getMessage() != null) {
            responseService.clearInlineMarkup(context, callbackQuery.getMessage().getMessageId(), "❌ Отменено");
        }

        // Also remove any reply keyboard if was in photo collection
        telegramSender.sendMessage(
                context,
                "Главное меню:",
                keyboardFactory.createRemoveReplyKeyboard()
        );

        telegramSender.sendMessage(
                context,
                "👋 **Главное меню Burmalda AI Video**\n\nВыберите действие:",
                keyboardFactory.createMainMenu()
        );
    }
}
