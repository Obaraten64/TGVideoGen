package com.burmaldabot.telegram.keyboard;

import com.burmaldabot.flow.UserFlowState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardServiceTest {

    private KeyboardService keyboardService;
    private TelegramKeyboardFactory factory;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        KeyboardTemplateEngine templateEngine = new KeyboardTemplateEngine();
        keyboardService = new KeyboardService(objectMapper, templateEngine);
        keyboardService.init();

        factory = new TelegramKeyboardFactory(keyboardService);
    }

    @Test
    void testLoadMainMenuKeyboard() {
        InlineKeyboardMarkup markup = keyboardService.getInlineKeyboard("MAIN_MENU");
        assertNotNull(markup);
        assertEquals(3, markup.getKeyboard().size());

        // Row 1: Generate
        assertEquals("🎬 Сгенерировать видео", markup.getKeyboard().get(0).get(0).getText());
        assertEquals("menu:generate", markup.getKeyboard().get(0).get(0).getCallbackData());

        // Row 3: Profile and Characters
        assertEquals(2, markup.getKeyboard().get(2).size());
        assertEquals("👤 Профиль", markup.getKeyboard().get(2).get(0).getText());
        assertEquals("🎭 Персонажи", markup.getKeyboard().get(2).get(1).getText());
    }

    @Test
    void testPhotoCollectionReplyKeyboardCondition() {
        // When count = 0, confirm button condition "count > 0" fails -> only Cancel button row
        ReplyKeyboardMarkup zeroPhotos = keyboardService.getReplyKeyboard("COLLECTING_PHOTOS", Map.of("count", 0));
        assertNotNull(zeroPhotos);
        assertEquals(1, zeroPhotos.getKeyboard().size());
        assertEquals("❌ Отмена", zeroPhotos.getKeyboard().get(0).get(0).getText());

        // When count = 4, confirm button condition passes -> 2 rows
        ReplyKeyboardMarkup fourPhotos = keyboardService.getReplyKeyboard("COLLECTING_PHOTOS", Map.of("count", 4));
        assertNotNull(fourPhotos);
        assertEquals(2, fourPhotos.getKeyboard().size());
        assertEquals("✅ Подтвердить (4 фото)", fourPhotos.getKeyboard().get(0).get(0).getText());
        assertEquals("❌ Отмена", fourPhotos.getKeyboard().get(1).get(0).getText());
    }

    @Test
    void testTopupConfirmationKeyboardFromJson() {
        InlineKeyboardMarkup markup = factory.createTopupConfirmationKeyboard(new BigDecimal("2.50"), "USDT");
        assertNotNull(markup);
        assertEquals(2, markup.getKeyboard().size());

        // Row 1: Confirm button with dynamic amount
        assertEquals("✅ Подтвердить и оплатить (2.50 USDT)", markup.getKeyboard().get(0).get(0).getText());
        assertEquals("topup:confirm", markup.getKeyboard().get(0).get(0).getCallbackData());

        // Row 2: Change amount and cancel
        assertEquals(2, markup.getKeyboard().get(1).size());
        assertEquals("✏️ Изменить количество", markup.getKeyboard().get(1).get(0).getText());
        assertEquals("profile:topup", markup.getKeyboard().get(1).get(0).getCallbackData());
        assertEquals("❌ Отмена", markup.getKeyboard().get(1).get(1).getText());
        assertEquals("menu:profile", markup.getKeyboard().get(1).get(1).getCallbackData());
    }

    @Test
    void testTopupInvoiceKeyboardFromJson() {
        InlineKeyboardMarkup markup = factory.createInvoiceKeyboard(
                "https://t.me/CryptoBot?start=test",
                new BigDecimal("5.00"),
                "USDT",
                "order-1234"
        );
        assertNotNull(markup);
        assertEquals(3, markup.getKeyboard().size());

        // Row 1: URL payment button
        assertEquals("💳 Оплатить 5.00 USDT в @CryptoBot", markup.getKeyboard().get(0).get(0).getText());
        assertEquals("https://t.me/CryptoBot?start=test", markup.getKeyboard().get(0).get(0).getUrl());

        // Row 2: Check status
        assertEquals("🔄 Проверить статус оплаты", markup.getKeyboard().get(1).get(0).getText());
        assertEquals("topup:check:order-1234", markup.getKeyboard().get(1).get(0).getCallbackData());
    }

    @Test
    void testRemoveKeyboard() {
        ReplyKeyboardRemove remove = keyboardService.getRemoveKeyboard();
        assertNotNull(remove);
        assertTrue(remove.getRemoveKeyboard());
    }

    @Test
    void testFactoryDelegation() {
        InlineKeyboardMarkup resMarkup = factory.createResolutionKeyboard();
        assertNotNull(resMarkup);
        assertEquals(4, resMarkup.getKeyboard().size());

        InlineKeyboardMarkup durationMarkup = factory.createDurationKeyboard();
        assertNotNull(durationMarkup);
        assertEquals(2, durationMarkup.getKeyboard().size());
    }

    @Test
    void testGetKeyboardByUserFlowState() {
        assertNotNull(keyboardService.getKeyboard(UserFlowState.SELECTING_RESOLUTION));
        assertNotNull(keyboardService.getKeyboard(UserFlowState.INPUTTING_DURATION));
    }
}
