package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.economy.pricing.PricingConfig;
import com.burmaldabot.flow.economy.pricing.TokenPricingService;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SelectTopupPackageCallbackHandler implements CallbackQueryHandler {

    private final TokenPricingService pricingService;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("topup:amount:");
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.AWAITING_TOPUP_AMOUNT, UserFlowState.MAIN_MENU);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long tokens;
        try {
            tokens = Long.parseLong(data.substring("topup:amount:".length()));
        } catch (NumberFormatException e) {
            log.warn("Invalid tokens amount in callback: {}", data);
            return;
        }

        PricingConfig config = pricingService.getConfig();
        BigDecimal totalPrice = pricingService.calculatePrice(tokens);

        session.setPendingTopupTokens(tokens);
        session.setState(UserFlowState.CONFIRMING_TOPUP);
        sessionService.save(session);

        if (callbackQuery.getMessage() != null) {
            responseService.clearInlineMarkup(
                    context,
                    callbackQuery.getMessage().getMessageId(),
                    String.format("Выбрано токенов: **%d токенов**", tokens)
            );
        }

        String confirmationText = String.format(
                "🧾 **Подтверждение заказа**\n\n" +
                        "• Выбрано токенов: **%d токенов**\n" +
                        "• Курс: **%s %s** за токен\n" +
                        "• Итого к оплате: **%s %s**\n\n" +
                        "Нажмите кнопку ниже, чтобы перейти к оплате через официального бота **@CryptoBot**:",
                tokens,
                config.getBasePricePerToken().toPlainString(),
                config.getAsset(),
                totalPrice.toPlainString(),
                config.getAsset()
        );

        telegramSender.sendMessage(
                context,
                confirmationText,
                keyboardFactory.createTopupConfirmationKeyboard(totalPrice, config.getAsset())
        );
    }
}
