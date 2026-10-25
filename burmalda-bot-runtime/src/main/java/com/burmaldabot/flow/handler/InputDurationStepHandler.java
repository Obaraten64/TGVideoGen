package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.UserFlowStepHandler;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.economy.VideoPricing;
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
public class InputDurationStepHandler implements UserFlowStepHandler {
    private static final String VALID_SECONDS_REGEX = "^([1-9]|[1-2][0-9]|30)$";

    private final UserSessionService sessionService;
    private final UserBalanceService balanceService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    @Override
    public UserFlowState getSupportedState() {
        return UserFlowState.INPUTTING_DURATION;
    }

    @Override
    public void handleMessage(BotContext context, BotUserSession session, Message message) {
        if (!message.hasText()) {
            sendValidationError(context);
            return;
        }

        String text = message.getText().trim();
        if (!text.matches(VALID_SECONDS_REGEX)) {
            sendValidationError(context);
            return;
        }
        int seconds = Integer.parseInt(text);

        processDuration(context, session, seconds);
    }

    public void processDuration(BotContext context, BotUserSession session, int seconds) {
        String resolution = session.getSelectedResolution();
        long totalCost = VideoPricing.calculateTotalCost(resolution, seconds, Boolean.TRUE.equals(session.getIsFreeTrial()));
        long currentBalance = balanceService.getBalance(context.telegramUserId(), context.bot().getId());

        if (currentBalance < totalCost && !Boolean.TRUE.equals(session.getIsFreeTrial())) {
            responseService.sendValidationError(
                    context,
                    String.format(
                            """
                             **Недостаточно токенов!**
                             
                             Стоимость: **%d токенов** (%d сек × %d ток/с)
                             Ваш баланс: **%d токенов**
                             
                             Пополните баланс в профиле или выберите меньшую длительность:""",
                            totalCost,
                            seconds,
                            VideoPricing.getRatePerSecond(resolution),
                            currentBalance
                    ),
                    keyboardFactory.createDurationKeyboard()
            );
            return;
        }

        session.setSelectedDuration(seconds);
        session.setState(UserFlowState.COLLECTING_PHOTOS);
        sessionService.save(session);

        telegramSender.sendMessage(
                context,
                String.format(
                        """
                        ✅ Длительность: **%d сек** (стоимость: %d токенов).
                        
                        📸 **Шаг 3 из 3: Отправьте фото (до 10 шт)**
                        Вы можете отправить до 10 фотографий (по одной или альбомом).
                        Когда закончите, нажмите кнопку «Подтвердить фото» внизу:""",
                        seconds,
                        totalCost
                ),
                keyboardFactory.createPhotoCollectionReplyKeyboard(0)
        );
    }

    private void sendValidationError(BotContext context) {
        telegramSender.sendMessage(
                context,
                "Пожалуйста, введите длительность видео числом от 1 до 30 секунд или выберите кнопку ниже:",
                keyboardFactory.createDurationKeyboard()
        );
    }
}
