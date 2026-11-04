package com.burmaldabot.telegram.keyboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;

/**
 * Backward-compatible facade delegating declarative keyboard construction to {@link KeyboardService}.
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
}
