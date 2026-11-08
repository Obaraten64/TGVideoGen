package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.UserFlowStepHandler;
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
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputTopupAmountStepHandler implements UserFlowStepHandler {

    private final TokenPricingService pricingService;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    @Override
    public UserFlowState getSupportedState() {
        return UserFlowState.AWAITING_TOPUP_AMOUNT;
    }

    @Override
    public void handleMessage(BotContext context, BotUserSession session, Message message) {
        PricingConfig config = pricingService.getConfig();

        if (!message.hasText()) {
            responseService.sendValidationError(
                    context,
                    "Пожалуйста, введите количество токенов числом или выберите пакет кнопкой ниже:",
                    keyboardFactory.createTopupPackagesKeyboard(config, pricingService)
            );
            return;
        }

        String text = message.getText().trim();
        if (text.contains("Отмена") || text.equalsIgnoreCase("/cancel")) {
            session.resetToMenu();
            sessionService.save(session);
            telegramSender.sendMessage(
                    context,
                    "❌ Пополнение баланса отменено.\n\nГлавное меню:",
                    keyboardFactory.createMainMenu()
            );
            return;
        }

        long tokens;
        try {
            tokens = Long.parseLong(text);
        } catch (NumberFormatException e) {
            responseService.sendValidationError(
                    context,
                    String.format("Некорректное число. Введите целое число токенов от %d до %d (например: 250):",
                            config.getMinTokens(), config.getMaxTokens()),
                    keyboardFactory.createTopupPackagesKeyboard(config, pricingService)
            );
            return;
        }

        if (tokens < config.getMinTokens() || tokens > config.getMaxTokens()) {
            responseService.sendValidationError(
                    context,
                    String.format("Количество токенов должно быть от %d до %d. Попробуйте еще раз:",
                            config.getMinTokens(), config.getMaxTokens()),
                    keyboardFactory.createTopupPackagesKeyboard(config, pricingService)
            );
            return;
        }

        BigDecimal totalPrice = pricingService.calculatePrice(tokens);

        session.setPendingTopupTokens(tokens);
        session.setState(UserFlowState.CONFIRMING_TOPUP);
        sessionService.save(session);

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
