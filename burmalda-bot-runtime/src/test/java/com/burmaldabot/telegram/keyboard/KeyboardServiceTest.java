package com.burmaldabot.telegram.keyboard;

import com.burmaldabot.flow.UserFlowState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

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
