package com.burmaldabot.telegram.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramKeyboardFactory {

    public InlineKeyboardMarkup createMainMenu() {
        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🎬 Сгенерировать видео").callbackData("menu:generate").build()
        );
        InlineKeyboardRow row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🎁 Бесплатная генерация (1 раз)").callbackData("menu:free_trial").build()
        );
        InlineKeyboardRow row3 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("👤 Профиль").callbackData("menu:profile").build(),
                InlineKeyboardButton.builder().text("🎭 Персонажи").callbackData("menu:characters").build()
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public InlineKeyboardMarkup createProfileKeyboard(boolean freeTrialAvailable) {
        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("💳 Пополнить баланс").callbackData("profile:topup").build()
        );
        InlineKeyboardRow row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🎞 Мои генерации").callbackData("profile:my_generations").build()
        );
        InlineKeyboardRow row3 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("⬅️ Главное меню").callbackData("menu:main").build()
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public InlineKeyboardMarkup createResolutionKeyboard() {
        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🔹 480p — 14 токенов/с").callbackData("gen:res:480p").build()
        );
        InlineKeyboardRow row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🔷 720p — 28 токенов/с").callbackData("gen:res:720p").build()
        );
        InlineKeyboardRow row3 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🔶 1080p — 56 токенов/с").callbackData("gen:res:1080p").build()
        );
        InlineKeyboardRow row4 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("❌ Отмена").callbackData("gen:cancel").build()
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3, row4))
                .build();
    }

    public InlineKeyboardMarkup createDurationKeyboard() {
        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("3 сек").callbackData("gen:dur:3").build(),
                InlineKeyboardButton.builder().text("5 сек").callbackData("gen:dur:5").build(),
                InlineKeyboardButton.builder().text("10 сек").callbackData("gen:dur:10").build(),
                InlineKeyboardButton.builder().text("15 сек").callbackData("gen:dur:15").build()
        );
        InlineKeyboardRow row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("❌ Отмена").callbackData("gen:cancel").build()
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();
    }

    public ReplyKeyboardMarkup createPhotoCollectionReplyKeyboard(int currentCount) {
        List<KeyboardRow> rows = new ArrayList<>();
        if (currentCount > 0) {
            KeyboardRow confirmRow = new KeyboardRow(
                    new KeyboardButton(String.format("✅ Подтвердить (%d фото)", currentCount))
            );
            rows.add(confirmRow);
        }
        KeyboardRow cancelRow = new KeyboardRow(
                new KeyboardButton("❌ Отмена")
        );
        rows.add(cancelRow);

        return ReplyKeyboardMarkup.builder()
                .keyboard(rows)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }

    public ReplyKeyboardRemove createRemoveReplyKeyboard() {
        return ReplyKeyboardRemove.builder()
                .removeKeyboard(true)
                .build();
    }

    public InlineKeyboardMarkup createPromptTemplatesKeyboard() {
        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🌆 Киберпанк").callbackData("tpl:cyberpunk").build(),
                InlineKeyboardButton.builder().text("🎬 Кинематограф").callbackData("tpl:cinematic").build()
        );
        InlineKeyboardRow row2 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🎌 Аниме").callbackData("tpl:anime").build(),
                InlineKeyboardButton.builder().text("✨ Сюрреализм").callbackData("tpl:surreal").build()
        );
        InlineKeyboardRow row3 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("❌ Отмена").callbackData("gen:cancel").build()
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();
    }

    public InlineKeyboardMarkup createGenerationStatusKeyboard() {
        InlineKeyboardRow row1 = new InlineKeyboardRow(
                InlineKeyboardButton.builder().text("🎞 Мои генерации").callbackData("profile:my_generations").build(),
                InlineKeyboardButton.builder().text("➕ Новая генерация").callbackData("menu:generate").build()
        );

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1))
                .build();
    }
}
