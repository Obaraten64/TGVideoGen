package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.UserFlowStepHandler;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputPromptStepHandler implements UserFlowStepHandler {

    private final VideoGenerationDispatcherService dispatcherService;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public UserFlowState getSupportedState() {
        return UserFlowState.INPUTTING_PROMPT;
    }

    @Override
    public void handleMessage(BotContext context, BotUserSession session, Message message) {
        if (!message.hasText()) {
            telegramSender.sendMessage(
                    context,
                    "⚠️ Пожалуйста, введите текстовый промпт сообщением или выберите шаблон стиля кнопкой выше.",
                    keyboardFactory.createPromptTemplatesKeyboard()
            );
            return;
        }

        String prompt = message.getText().trim();
        if (prompt.contains("Отмена")) {
            session.resetToMenu();
            sessionService.save(session);
            telegramSender.sendMessage(
                    context,
                    "❌ Создание видео отменено.\n\nГлавное меню:",
                    keyboardFactory.createMainMenu()
            );
            return;
        }

        dispatcherService.startGeneration(context, session, prompt);
    }
}
