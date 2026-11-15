package com.burmaldabot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("ru");

    /**
     * Resolves a static message with no arguments.
     *
     * @param code the message code
     * @return the resolved message text, or the code if not found
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, code, DEFAULT_LOCALE);
    }

    /**
     * Resolves a message template with dynamic positional arguments ({0}, {1}, etc.).
     *
     * @param code the message code
     * @param args the arguments to substitute into the template
     * @return the resolved and formatted message text, or the code if not found
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, code, DEFAULT_LOCALE);
    }
}
