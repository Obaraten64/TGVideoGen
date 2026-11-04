package com.burmaldabot.telegram.keyboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardTemplateEngineTest {

    private KeyboardTemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new KeyboardTemplateEngine();
    }

    @Test
    void testEvaluateTextWithPlaceholders() {
        String template = "Подтвердить ({count} фото) для пользователя {user}";
        Map<String, Object> params = Map.of("count", 5, "user", "Alex");

        assertEquals("Подтвердить (5 фото) для пользователя Alex", engine.evaluateText(template, params));
    }

    @Test
    void testEvaluateTextWithoutPlaceholders() {
        assertEquals("Простой текст", engine.evaluateText("Простой текст", Map.of("count", 1)));
        assertEquals("Простой текст", engine.evaluateText("Простой текст", null));
    }

    @Test
    void testEvaluateConditionEmptyOrNull() {
        assertTrue(engine.evaluateCondition(null, Map.of()));
        assertTrue(engine.evaluateCondition("", Map.of()));
        assertTrue(engine.evaluateCondition("   ", Map.of()));
    }

    @Test
    void testEvaluateConditionNumericComparison() {
        Map<String, Object> params = Map.of("count", 3);

        assertTrue(engine.evaluateCondition("count > 0", params));
        assertTrue(engine.evaluateCondition("count >= 3", params));
        assertTrue(engine.evaluateCondition("count < 5", params));
        assertTrue(engine.evaluateCondition("count <= 3", params));
        assertTrue(engine.evaluateCondition("count == 3", params));
        assertFalse(engine.evaluateCondition("count != 3", params));
        assertFalse(engine.evaluateCondition("count > 3", params));
    }

    @Test
    void testEvaluateConditionBooleanFlags() {
        Map<String, Object> params = Map.of("isAvailable", true, "isExpired", false);

        assertTrue(engine.evaluateCondition("isAvailable", params));
        assertFalse(engine.evaluateCondition("isExpired", params));
        assertTrue(engine.evaluateCondition("!isExpired", params));
        assertFalse(engine.evaluateCondition("!isAvailable", params));
    }
}
