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
public class SelectResolutionCallbackHandler implements CallbackQueryHandler {

    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("gen:res:");
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.SELECTING_RESOLUTION);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String resolution = data.substring("gen:res:".length());

        session.setSelectedResolution(resolution);
        session.setState(UserFlowState.INPUTTING_DURATION);
        sessionService.save(session);

        // Edit original message to remove stale resolution buttons and show choice
        if (callbackQuery.getMessage() != null) {
            responseService.clearInlineMarkup(
                    context,
                    callbackQuery.getMessage().getMessageId(),
                    String.format("✅ Выбрано качество: **%s**", resolution)
            );
        }

        telegramSender.sendMessage(
                context,
                """
                        ⏱ **Шаг 2 из 3: Укажите длительность видео**
                        
                        Введите количество секунд (от **1** до **30**) числом в чат или выберите быстрый вариант ниже:""",
                keyboardFactory.createDurationKeyboard()
        );
    }
}
