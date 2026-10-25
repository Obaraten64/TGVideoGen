package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
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
public class StartGenerateCallbackHandler implements CallbackQueryHandler {

    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return "menu:generate".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.MAIN_MENU);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        session.startPaidGeneration();
        sessionService.save(session);

        telegramSender.sendMessage(
                context,
                """
                        🎬 **Шаг 1 из 3: Выберите качество видео**
                        
                        Чем выше качество, тем больше токенов потребуется на генерацию:
                        • 480p — 14 токенов/сек
                        • 720p — 28 токенов/сек
                        • 1080p — 56 токенов/сек""",
                keyboardFactory.createResolutionKeyboard()
        );
    }
}
