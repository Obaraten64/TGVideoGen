package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.UserFlowStepHandler;
import com.burmaldabot.flow.economy.pricing.PricingConfig;
import com.burmaldabot.flow.economy.pricing.TokenPricingService;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.service.MessageService;
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
    private final MessageService messageService;

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
                    messageService.getMessage("topup.input.prompt"),
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
                    messageService.getMessage("topup.input.cancelled"),
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
                    messageService.getMessage("topup.input.invalid_number", config.getMinTokens(), config.getMaxTokens()),
                    keyboardFactory.createTopupPackagesKeyboard(config, pricingService)
            );
            return;
        }

        if (tokens < config.getMinTokens() || tokens > config.getMaxTokens()) {
            responseService.sendValidationError(
                    context,
                    messageService.getMessage("topup.input.out_of_range", config.getMinTokens(), config.getMaxTokens()),
                    keyboardFactory.createTopupPackagesKeyboard(config, pricingService)
            );
            return;
        }

        BigDecimal totalPrice = pricingService.calculatePrice(tokens);

        session.setPendingTopupTokens(tokens);
        session.setState(UserFlowState.CONFIRMING_TOPUP);
        sessionService.save(session);

        String confirmationText = messageService.getMessage(
                "topup.confirmation.card",
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
