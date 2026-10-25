package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.UserBalanceService;
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
public class StartFreeTrialCallbackHandler implements CallbackQueryHandler {

    private final UserSessionService sessionService;
    private final UserBalanceService balanceService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return "menu:free_trial".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.MAIN_MENU);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        boolean used = balanceService.hasUsedFreeTrial(context.telegramUserId(), context.bot().getId());
        if (used) {
            telegramSender.sendMessage(
                    context,
                    """
                            ⚠️ **Бесплатная генерация уже использована!**
                            
                            Вы можете пополнить баланс в профиле и создавать видео в любом разрешении.""",
                    keyboardFactory.createProfileKeyboard(false)
            );
            return;
        }

        session.startFreeTrial();
        sessionService.save(session);

        telegramSender.sendMessage(
                context,
                """
                        🎁 **Бесплатная пробная генерация активирована!**
                        
                        Параметры: **480p, 3 секунды** (0 токенов).
                        
                        📸 **Отправьте фото (до 10 шт)**. Отправляйте по одному или альбомом.
                        После отправки нажмите кнопку «Подтвердить фото» под строкой ввода.""",
                keyboardFactory.createPhotoCollectionReplyKeyboard(0)
        );
    }
}
