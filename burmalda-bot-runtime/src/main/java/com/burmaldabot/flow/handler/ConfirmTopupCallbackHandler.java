package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.payment.PaymentOrder;
import com.burmaldabot.flow.economy.payment.PaymentOrderService;
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
public class ConfirmTopupCallbackHandler implements CallbackQueryHandler {

    private final PaymentOrderService paymentOrderService;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    @Override
    public boolean supports(String callbackData) {
        return "topup:confirm".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.CONFIRMING_TOPUP);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        Long tokens = session.getPendingTopupTokens();
        if (tokens == null || tokens <= 0) {
            log.warn("No pending tokens found for confirmation in user session {}", session.getId());
            session.resetToMenu();
            sessionService.save(session);
            telegramSender.sendMessage(context, "⚠️ Сессия заказа истекла. Пожалуйста, начните заново.", keyboardFactory.createMainMenu());
            return;
        }

        try {
            PaymentOrder order = paymentOrderService.createInvoiceOrder(context, tokens);

            session.resetToMenu();
            sessionService.save(session);

            if (callbackQuery.getMessage() != null) {
                responseService.clearInlineMarkup(
                        context,
                        callbackQuery.getMessage().getMessageId(),
                        "✅ Заказ подтвержден. Ссылка на оплату сформирована."
                );
            }

            String invoiceMessage = String.format(
                    "💳 **Счет на оплату сформирован!**\n\n" +
                            "• Сумма к оплате: **%s %s**\n" +
                            "• К зачислению: **+%d токенов**\n" +
                            "• Срок действия счета: **30 минут**\n\n" +
                            "Нажмите кнопку ниже, чтобы оплатить заказ через официального бота **@CryptoBot**.\n" +
                            "После завершения платежа токены поступят на ваш баланс автоматически!",
                    order.getPriceAmount().toPlainString(),
                    order.getAsset(),
                    order.getTokensAmount()
            );

            telegramSender.sendMessage(
                    context,
                    invoiceMessage,
                    keyboardFactory.createInvoiceKeyboard(order.getPayUrl(), order.getPriceAmount(), order.getAsset(), order.getOrderUuid())
            );

        } catch (Exception e) {
            log.error("Failed to create Crypto Pay invoice order", e);
            telegramSender.sendMessage(
                    context,
                    "❌ Произошла ошибка при создании счета на оплату. Попробуйте еще раз позже.",
                    keyboardFactory.createMainMenu()
            );
        }
    }
}
