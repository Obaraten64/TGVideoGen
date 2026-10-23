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

@Component
@RequiredArgsConstructor
@Slf4j
public class SelectResolutionCallbackHandler implements CallbackQueryHandler {

    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && (callbackData.startsWith("gen:res:") || "gen:cancel".equals(callbackData));
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        if ("gen:cancel".equals(data)) {
            session.resetToMenu();
            sessionService.save(session);
            telegramSender.sendMessage(
                    context,
                    "❌ Действие отменено.\n\nГлавное меню:",
                    keyboardFactory.createMainMenu()
            );
            return;
        }

        String resolution = data.substring("gen:res:".length());
        session.setSelectedResolution(resolution);
        session.setState(UserFlowState.INPUTTING_DURATION);
        sessionService.save(session);

        telegramSender.sendMessage(
                context,
                String.format(
                        "✅ Выбрано качество: **%s**\n\n" +
                                "⏱ **Шаг 2 из 3: Укажите длительность видео**\n" +
                                "Введите количество секунд (от **1** до **30**) числом в чат или выберите быстрый вариант:",
                        resolution
                ),
                keyboardFactory.createDurationKeyboard()
        );
    }
}
