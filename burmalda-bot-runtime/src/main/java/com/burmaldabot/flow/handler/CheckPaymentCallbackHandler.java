package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.payment.PaymentOrderService;
import com.burmaldabot.flow.economy.payment.PaymentStatus;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
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

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("topup:check:");
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(); // Allowed from any state
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String orderUuid = data.substring("topup:check:".length());

        PaymentStatus status = paymentOrderService.checkAndSyncStatus(context, orderUuid);

        switch (status) {
            case PAID -> telegramSender.answerCallbackQuery(
                    context,
                    callbackQuery.getId(),
                    "✅ Оплата подтверждена! Токены уже зачислены на ваш баланс.",
                    true
            );
            case PENDING -> telegramSender.answerCallbackQuery(
                    context,
                    callbackQuery.getId(),
                    "⏳ Платеж еще не поступил. Пожалуйста, завершите оплату в @CryptoBot.",
                    true
            );
            case EXPIRED -> telegramSender.answerCallbackQuery(
                    context,
                    callbackQuery.getId(),
                    "⚠️ Срок действия этого счета истек. Создайте новый заказ в профиле.",
                    true
            );
            default -> telegramSender.answerCallbackQuery(
                    context,
                    callbackQuery.getId(),
                    "⚠️ Заказ не найден или отменен.",
                    true
            );
        }
    }
}
