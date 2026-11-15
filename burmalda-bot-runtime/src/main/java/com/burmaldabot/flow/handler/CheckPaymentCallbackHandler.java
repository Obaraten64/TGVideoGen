package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.economy.payment.PaymentOrderService;
import com.burmaldabot.flow.economy.payment.PaymentStatus;
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
public class CheckPaymentCallbackHandler implements CallbackQueryHandler {

    private final PaymentOrderService paymentOrderService;
    private final TelegramSender telegramSender;
    private final UserBalanceService balanceService;
    private final TelegramKeyboardFactory keyboardFactory;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("topup:check:");
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(); // Allowed from any state
    }

    @Override
    public boolean answersCallbackQuery() {
        return true;
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String orderUuid = data.substring("topup:check:".length());

        PaymentStatus status = paymentOrderService.checkAndSyncStatus(context, orderUuid);

        switch (status) {
            case PAID -> {
                long currentBalance = balanceService.getBalance(context.telegramUserId(), context.bot().getId());
                telegramSender.sendMessage(
                        context,
                        String.format(
                                "🎉 **Оплата успешно подтверждена!**\n\n" +
                                        "Текущий баланс: **%d токенов**\n\n" +
                                        "Приятного создания видео!",
                                currentBalance
                        ),
                        keyboardFactory.createMainMenu()
                );
                session.resetToMenu();
            }
            case PENDING -> {
                telegramSender.sendMessage(
                        context,
                        "⏳ **Платеж еще не поступил.**\n\n" +
                                "Пожалуйста, перейдите в @CryptoBot по ссылке в сообщении выше и завершите оплату. " +
                                "После оплаты нажмите кнопку **«🔄 Проверить статус оплаты»** снова.",
                        null
                );
            }
            case EXPIRED -> {
                telegramSender.sendMessage(
                        context,
                        "⚠️ **Срок действия этого счета истек.**\n\n" +
                                "Пожалуйста, вернитесь в главное меню и сформируйте новый счет при необходимости.",
                        keyboardFactory.createMainMenu()
                );
                session.resetToMenu();
            }
            default -> {
                telegramSender.sendMessage(
                        context,
                        "⚠️ **Заказ не найден или отменен.**\n\n" +
                                "Пожалуйста, вернитесь в главное меню.",
                        keyboardFactory.createMainMenu()
                );
                session.resetToMenu();
            }
        }
    }
}
