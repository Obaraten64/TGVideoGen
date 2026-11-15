package com.burmaldabot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("message");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true);

        messageService = new MessageService(messageSource);
    }

    @Test
    void testGetStaticMessage() {
        String welcome = messageService.getMessage("menu.main.header");
        assertEquals("Главное меню:", welcome);
    }

    @Test
    void testGetFormattedMessage() {
        String formatted = messageService.getMessage("flow.generate.photos.accepted", 5);
        assertEquals("✅ Принято 5 фото!", formatted);
    }

    @Test
    void testUserIdFormattedWithoutGroupingSeparators() {
        long userId = 123456789L;
        String profileInfo = messageService.getMessage("profile.info", String.valueOf(userId), 100, "Доступна ✅");
        assertTrue(profileInfo.contains("🆔 ID: `123456789`"), "User ID must not contain thousand separators");
    }

    @Test
    void testCyrillicEncodingIntegrity() {
        String staleAlert = messageService.getMessage("alert.stale_action");
        assertTrue(staleAlert.contains("Это действие устарело"));
        assertFalse(staleAlert.contains("?"), "Message should not contain unencoded question marks");
    }

    @Test
    void testMissingKeyReturnsCodeFallback() {
        String nonExistent = messageService.getMessage("non.existent.key");
        assertEquals("non.existent.key", nonExistent);
    }
}
