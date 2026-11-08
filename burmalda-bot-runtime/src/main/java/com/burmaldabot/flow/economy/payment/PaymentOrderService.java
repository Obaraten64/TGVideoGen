package com.burmaldabot.flow.economy.payment;

import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.economy.payment.dto.CreateInvoiceRequest;
import com.burmaldabot.flow.economy.payment.dto.CryptoInvoiceDto;
import com.burmaldabot.flow.economy.pricing.PricingConfig;
import com.burmaldabot.flow.economy.pricing.TokenPricingService;
import com.burmaldabot.model.bot.Bot;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.repository.BotRepository;
import com.burmaldabot.telegram.TelegramSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrderService {

    private final PaymentOrderRepository orderRepository;
    private final CryptoPayClient cryptoPayClient;
    private final TokenPricingService pricingService;
    private final UserBalanceService balanceService;
    private final BotRepository botRepository;
    private final TelegramSender telegramSender;

    @Transactional
    public PaymentOrder createInvoiceOrder(BotContext context, long tokens) {
        PricingConfig config = pricingService.getConfig();
        if (tokens < config.getMinTokens() || tokens > config.getMaxTokens()) {
            throw new IllegalArgumentException(String.format("Количество токенов должно быть от %d до %d",
                    config.getMinTokens(), config.getMaxTokens()));
        }

        BigDecimal totalPrice = pricingService.calculatePrice(tokens);
        String orderUuid = UUID.randomUUID().toString();

        PaymentOrder order = PaymentOrder.builder()
                .orderUuid(orderUuid)
                .botId(context.bot().getId())
                .telegramUserId(context.telegramUserId())
                .chatId(context.chatId())
                .tokensAmount(tokens)
                .priceAmount(totalPrice)
                .asset(config.getAsset())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        order = orderRepository.save(order);

        String botUsername = context.bot().getUsername();
        String botUrl = botUsername != null ? "https://t.me/" + botUsername : null;

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .asset(config.getAsset())
                .amount(totalPrice.toPlainString())
                .description(String.format("Пополнение баланса Burmalda AI — %d токенов", tokens))
                .payload(orderUuid)
                .paidBtnName(botUrl != null ? "openBot" : null)
                .paidBtnUrl(botUrl)
                .expiresIn(config.getInvoiceExpiresInSeconds())
                .build();

        CryptoInvoiceDto invoice = cryptoPayClient.createInvoice(request);
        order.setCryptoInvoiceId(invoice.getInvoiceId());
        order.setPayUrl(invoice.getBotInvoiceUrl() != null ? invoice.getBotInvoiceUrl() : invoice.getPayUrl());

        return orderRepository.save(order);
    }

    @Transactional
    public boolean fulfillInvoice(CryptoInvoiceDto invoiceDto) {
        Long cryptoInvoiceId = invoiceDto.getInvoiceId();
        String payloadUuid = invoiceDto.getPayload();

        Optional<PaymentOrder> orderOpt = cryptoInvoiceId != null
                ? orderRepository.findForUpdateByCryptoInvoiceId(cryptoInvoiceId)
                : Optional.empty();

        if (orderOpt.isEmpty() && payloadUuid != null) {
            orderOpt = orderRepository.findForUpdateByOrderUuid(payloadUuid);
        }

        if (orderOpt.isEmpty()) {
            log.warn("PaymentOrder not found for Crypto Pay invoiceId: {}, payload: {}", cryptoInvoiceId, payloadUuid);
            return false;
        }

        PaymentOrder order = orderOpt.get();

        // Idempotency: skip already paid orders
        if (order.getStatus() == PaymentStatus.PAID) {
            log.info("PaymentOrder {} already marked as PAID. Skipping duplicate fulfillment.", order.getOrderUuid());
            return true;
        }

        order.setStatus(PaymentStatus.PAID);
        order.setPaidAt(Instant.now());
        if (order.getCryptoInvoiceId() == null && cryptoInvoiceId != null) {
            order.setCryptoInvoiceId(cryptoInvoiceId);
        }
        orderRepository.save(order);

        // Credit tokens
        balanceService.addTokens(order.getTelegramUserId(), order.getBotId(), order.getTokensAmount());
        long newBalance = balanceService.getBalance(order.getTelegramUserId(), order.getBotId());

        log.info("Successfully fulfilled PaymentOrder {}: credited {} tokens to user {} on bot {}",
                order.getOrderUuid(), order.getTokensAmount(), order.getTelegramUserId(), order.getBotId());

        // Send confirmation message to Telegram user
        notifyUserPaymentSuccess(order, newBalance);
        return true;
    }

    @Transactional
    public PaymentStatus checkAndSyncStatus(BotContext context, String orderUuid) {
        Optional<PaymentOrder> orderOpt = orderRepository.findForUpdateByOrderUuid(orderUuid);
        if (orderOpt.isEmpty()) {
            return PaymentStatus.CANCELLED;
        }

        PaymentOrder order = orderOpt.get();
        if (order.getStatus() == PaymentStatus.PAID) {
            return PaymentStatus.PAID;
        }

        if (order.getCryptoInvoiceId() != null) {
            try {
                List<CryptoInvoiceDto> invoices = cryptoPayClient.getInvoices(List.of(order.getCryptoInvoiceId()));
                if (!invoices.isEmpty()) {
                    CryptoInvoiceDto remoteInvoice = invoices.get(0);
                    if ("paid".equalsIgnoreCase(remoteInvoice.getStatus())) {
                        fulfillInvoice(remoteInvoice);
                        return PaymentStatus.PAID;
                    } else if ("expired".equalsIgnoreCase(remoteInvoice.getStatus())) {
                        order.setStatus(PaymentStatus.EXPIRED);
                        orderRepository.save(order);
                        return PaymentStatus.EXPIRED;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to query Crypto Pay for order {}: {}", orderUuid, e.getMessage());
            }
        }

        return order.getStatus();
    }

    private void notifyUserPaymentSuccess(PaymentOrder order, long newBalance) {
        try {
            Optional<Bot> botOpt = botRepository.findById(order.getBotId());
            if (botOpt.isEmpty()) {
                return;
            }

            BotContext botContext = new BotContext(botOpt.get(), order.getChatId(), order.getTelegramUserId());
            String successMessage = String.format(
                    "🎉 **Оплата успешно получена!**\n\n" +
                            "На ваш баланс зачислено: **+%d токенов**\n" +
                            "Текущий баланс: **%d токенов**\n\n" +
                            "Приятного создания видео!",
                    order.getTokensAmount(),
                    newBalance
            );

            telegramSender.sendMessage(botContext, successMessage, null);
        } catch (Exception e) {
            log.error("Failed to send Telegram success notification for order {}", order.getOrderUuid(), e);
        }
    }
}
