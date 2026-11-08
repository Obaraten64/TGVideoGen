package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.session.BotUserSession;
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
public class ProfileMenuCallbackHandler implements CallbackQueryHandler {

    private final UserBalanceService balanceService;
    private final com.burmaldabot.flow.session.UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return "menu:profile".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.MAIN_MENU, UserFlowState.AWAITING_TOPUP_AMOUNT, UserFlowState.CONFIRMING_TOPUP);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        if (session.getState() != UserFlowState.MAIN_MENU) {
            session.resetToMenu();
            sessionService.save(session);
        }

        long balance = balanceService.getBalance(context.telegramUserId(), context.bot().getId());
        boolean freeTrialAvailable = !balanceService.hasUsedFreeTrial(context.telegramUserId(), context.bot().getId());

        String profileText = String.format(
                """
                        👤 **Ваш профиль**
                        
                        🆔 ID: `%d`
                        💰 Баланс: **%d токенов**
                        🎁 Бесплатная генерация: **%s**""",
                context.telegramUserId(),
                balance,
                freeTrialAvailable ? "Доступна ✅" : "Использована ❌"
        );

        telegramSender.sendMessage(
                context,
                profileText,
                keyboardFactory.createProfileKeyboard(freeTrialAvailable)
        );
    }
}
