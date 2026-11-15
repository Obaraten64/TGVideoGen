package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.payment.PaymentOrder;
import com.burmaldabot.flow.economy.payment.PaymentOrderService;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.service.MessageService;
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
    private final MessageService messageService;

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
            telegramSender.sendMessage(context, messageService.getMessage("topup.session_expired"), keyboardFactory.createMainMenu());
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
                        messageService.getMessage("topup.confirmed_inline")
                );
            }

            String invoiceMessage = messageService.getMessage(
                    "topup.invoice.created",
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
                    messageService.getMessage("topup.invoice.error"),
                    keyboardFactory.createMainMenu()
            );
        }
    }
}
