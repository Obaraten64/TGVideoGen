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
public class TopUpCallbackHandler implements CallbackQueryHandler {

    private final UserBalanceService balanceService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return "profile:topup".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.MAIN_MENU);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        balanceService.addTokens(context.telegramUserId(), context.bot().getId(), 100L);
        long newBalance = balanceService.getBalance(context.telegramUserId(), context.bot().getId());

        telegramSender.sendMessage(
                context,
                String.format(
                        """
                                💳 **Баланс пополнен!**
                                
                                Вам начислено +100 тестовых токенов.
                                Текущий баланс: **%d токенов**""",
                        newBalance
                ),
                keyboardFactory.createProfileKeyboard(!balanceService.hasUsedFreeTrial(context.telegramUserId(), context.bot().getId()))
        );
    }
}
