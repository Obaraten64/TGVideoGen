package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.UserFlowState;
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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopUpCallbackHandler implements CallbackQueryHandler {

    private final TokenPricingService pricingService;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final MessageService messageService;

    @Override
    public boolean supports(String callbackData) {
        return "profile:topup".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(UserFlowState.MAIN_MENU, UserFlowState.AWAITING_TOPUP_AMOUNT, UserFlowState.CONFIRMING_TOPUP);
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        PricingConfig config = pricingService.getConfig();

        session.setState(UserFlowState.AWAITING_TOPUP_AMOUNT);
        sessionService.save(session);

        String messageText = messageService.getMessage(
                "topup.menu.prompt",
                config.getBasePricePerToken().toPlainString(),
                config.getAsset(),
                config.getMinTokens(),
                config.getMaxTokens()
        );

        telegramSender.sendMessage(
                context,
                messageText,
                keyboardFactory.createTopupPackagesKeyboard(config, pricingService)
        );
    }
}
