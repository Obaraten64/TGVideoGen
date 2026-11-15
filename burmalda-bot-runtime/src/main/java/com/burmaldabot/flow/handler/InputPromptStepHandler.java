package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.UserFlowStepHandler;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.service.MessageService;
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
    private final FlowResponseService responseService;
    private final MessageService messageService;

    @Override
    public UserFlowState getSupportedState() {
        return UserFlowState.INPUTTING_PROMPT;
    }

    @Override
    public void handleMessage(BotContext context, BotUserSession session, Message message) {
        if (!message.hasText()) {
            responseService.sendValidationError(
                    context,
                    messageService.getMessage("flow.generate.prompt.invalid"),
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
                    messageService.getMessage("flow.generate.prompt.cancelled"),
                    keyboardFactory.createMainMenu()
            );
            return;
        }

        dispatcherService.startGeneration(context, session, prompt);
    }
}
