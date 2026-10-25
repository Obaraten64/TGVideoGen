package com.burmaldabot.telegram.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TelegramTextFormatterTest {
    private final TelegramTextFormatter formatter = new TelegramTextFormatter();

    @Test
    void shouldRenderBoldText() {
        String result = formatter.toTelegramHtml("**Hello**");

        assertThat(result).isEqualTo("<strong>Hello</strong>");
    }
}
