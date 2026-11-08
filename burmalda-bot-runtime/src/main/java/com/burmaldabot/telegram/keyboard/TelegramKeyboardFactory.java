package com.burmaldabot.telegram.keyboard;

import com.burmaldabot.flow.economy.pricing.FastButtonConfig;
import com.burmaldabot.flow.economy.pricing.PricingConfig;
import com.burmaldabot.flow.economy.pricing.TokenPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade delegating all keyboard construction to {@link KeyboardService} based on keyboards.json.
 */
@Component
@RequiredArgsConstructor
public class TelegramKeyboardFactory {

    private final KeyboardService keyboardService;

    public InlineKeyboardMarkup createMainMenu() {
        return keyboardService.getInlineKeyboard("MAIN_MENU");
    }

    public InlineKeyboardMarkup createProfileKeyboard(boolean freeTrialAvailable) {
        return keyboardService.getInlineKeyboard("PROFILE", Map.of("freeTrialAvailable", freeTrialAvailable));
    }

    public InlineKeyboardMarkup createResolutionKeyboard() {
        return keyboardService.getInlineKeyboard("SELECTING_RESOLUTION");
    }

    public InlineKeyboardMarkup createDurationKeyboard() {
        return keyboardService.getInlineKeyboard("INPUTTING_DURATION");
    }

    public ReplyKeyboardMarkup createPhotoCollectionReplyKeyboard(int currentCount) {
        return keyboardService.getReplyKeyboard("COLLECTING_PHOTOS", Map.of("count", currentCount));
    }

    public ReplyKeyboardRemove createRemoveReplyKeyboard() {
        return keyboardService.getRemoveKeyboard();
    }

    public InlineKeyboardMarkup createPromptTemplatesKeyboard() {
        return keyboardService.getInlineKeyboard("INPUTTING_PROMPT");
    }

    public InlineKeyboardMarkup createGenerationStatusKeyboard() {
        return keyboardService.getInlineKeyboard("GENERATION_STATUS");
    }

    public InlineKeyboardMarkup createTopupPackagesKeyboard(PricingConfig config, TokenPricingService pricingService) {
        Map<String, Object> params = new HashMap<>();

        if (config.getFastButtons() != null) {
            List<FastButtonConfig> btns = config.getFastButtons();
            for (int i = 0; i < btns.size() && i < 3; i++) {
                FastButtonConfig btn = btns.get(i);
                BigDecimal price = pricingService.calculateFastButtonPrice(btn);
                String discountBadge = btn.getDiscountPercent() > 0 ? String.format(" (-%d%%)", btn.getDiscountPercent()) : "";
                String text = String.format("💎 %d токенов — %s %s%s", btn.getTokens(), price.toPlainString(), config.getAsset(), discountBadge);

                int idx = i + 1;
                params.put("btn" + idx + "_text", text);
                params.put("btn" + idx + "_data", "topup:amount:" + btn.getTokens());
            }
        }

        return keyboardService.getInlineKeyboard("TOP_UP_PACKAGES", params);
    }

    public InlineKeyboardMarkup createTopupConfirmationKeyboard(BigDecimal price, String asset) {
        return keyboardService.getInlineKeyboard("TOP_UP_CONFIRMATION", Map.of(
                "price", price.toPlainString(),
                "asset", asset
        ));
    }

    public InlineKeyboardMarkup createInvoiceKeyboard(String payUrl, BigDecimal price, String asset, String orderUuid) {
        return keyboardService.getInlineKeyboard("TOP_UP_INVOICE", Map.of(
                "pay_url", payUrl,
                "price", price.toPlainString(),
                "asset", asset,
                "order_uuid", orderUuid
        ));
    }
}
