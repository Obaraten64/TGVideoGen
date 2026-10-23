package com.burmaldabot.flow;

import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFlowDispatcher {

    private final List<UserFlowStepHandler> stepHandlers;
    private final List<CallbackQueryHandler> callbackHandlers;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    public void dispatchMessage(BotContext context, Message message) {
        BotUserSession session = sessionService.getOrCreate(context);

        // Global commands check
        if (message.hasText()) {
            String text = message.getText().trim();
            if ("/start".equalsIgnoreCase(text) || "/menu".equalsIgnoreCase(text) || "❌ Отмена".equalsIgnoreCase(text)) {
                session.resetToMenu();
                sessionService.save(session);
                telegramSender.sendMessage(
                        context,
                        "Главное меню:",
                        keyboardFactory.createRemoveReplyKeyboard()
                );
                telegramSender.sendMessage(
                        context,
                        """
                                👋 **Добро пожаловать в Burmalda AI Video!**
                                
                                Создавайте захватывающие видеоролики из ваших фото с помощью нейросетей Wan 3.0.
                                Выберите действие:""",
                        keyboardFactory.createMainMenu()
                );
                return;
            }
        }

        UserFlowState currentState = session.getState();
        log.info("Dispatching message for user {} in state {}", context.telegramUserId(), currentState);

        UserFlowStepHandler handler = stepHandlers.stream()
                .filter(h -> h.getSupportedState() == currentState)
                .findFirst()
                .orElse(null);

        if (handler != null) {
            handler.handleMessage(context, session, message);
        } else {
            // Default response when in MAIN_MENU or unexpected state
            telegramSender.sendMessage(
                    context,
                    "👋 **Добро пожаловать в Burmalda AI Video!**\n\nВыберите действие в меню ниже:",
                    keyboardFactory.createMainMenu()
            );
        }
    }

    public void dispatchCallback(BotContext context, CallbackQuery callbackQuery) {
        BotUserSession session = sessionService.getOrCreate(context);
        String data = callbackQuery.getData();
        log.info("Dispatching callback query '{}' for user {}", data, context.telegramUserId());

        // Stop button loading animation in Telegram client
        telegramSender.answerCallbackQuery(context, callbackQuery.getId(), null);

        CallbackQueryHandler handler = callbackHandlers.stream()
                .filter(h -> h.supports(data))
                .findFirst()
                .orElse(null);

        if (handler != null) {
            handler.handleCallback(context, session, callbackQuery);
        } else {
            log.warn("No handler found for callback data: {}", data);
        }
    }
}
