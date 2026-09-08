package com.burmaldabot.exception;

public class TelegramFileException extends RuntimeException {
    public TelegramFileException(String message) {
        super(message);
    }

    public TelegramFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
