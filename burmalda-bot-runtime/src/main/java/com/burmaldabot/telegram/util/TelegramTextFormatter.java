package com.burmaldabot.telegram.util;

/**
 * A utility class for formatting text in Telegram messages.
 */
public final class TelegramTextFormatter {

    private TelegramTextFormatter() {
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String bold(String text) {
        return "<b>" + escape(text) + "</b>";
    }

    public static String italic(String text) {
        return "<i>" + escape(text) + "</i>";
    }

    public static String underline(String text) {
        return "<u>" + escape(text) + "</u>";
    }

    public static String strikethrough(String text) {
        return "<s>" + escape(text) + "</s>";
    }

    public static String code(String text) {
        return "<code>" + escape(text) + "</code>";
    }

    public static String link(String text, String url) {
        return "<a href=\"" + escape(url) + "\">"
                + escape(text)
                + "</a>";
    }
}