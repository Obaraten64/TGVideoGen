package com.burmaldabot.exception;

public class KieApiException extends RuntimeException {
    public KieApiException(String message) {
        super(message);
    }
    public KieApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
