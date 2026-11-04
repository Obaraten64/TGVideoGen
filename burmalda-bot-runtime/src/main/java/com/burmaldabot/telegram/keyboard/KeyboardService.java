package com.burmaldabot.telegram.keyboard;

import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.telegram.keyboard.config.ButtonConfig;
import com.burmaldabot.telegram.keyboard.config.KeyboardConfigFile;
import com.burmaldabot.telegram.keyboard.config.KeyboardDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyboardService {

    private static final String CONFIG_PATH = "keyboards.json";

    private final ObjectMapper objectMapper;
    private final KeyboardTemplateEngine templateEngine;

    private Map<String, KeyboardDefinition> registry = new HashMap<>();

    @PostConstruct
    public void init() {
        loadConfiguration();
    }

    public synchronized void loadConfiguration() {
        try {
            ClassPathResource resource = new ClassPathResource(CONFIG_PATH);
            try (InputStream is = resource.getInputStream()) {
                KeyboardConfigFile config = objectMapper.readValue(is, KeyboardConfigFile.class);
                if (config != null && config.getKeyboards() != null) {
                    this.registry = Collections.unmodifiableMap(new HashMap<>(config.getKeyboards()));
                    log.info("Successfully loaded {} keyboard stage definitions from {}", registry.size(), CONFIG_PATH);
                } else {
                    log.warn("Empty keyboard configuration loaded from {}", CONFIG_PATH);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load keyboard configuration from {}: {}", CONFIG_PATH, e.getMessage(), e);
            throw new IllegalStateException("Could not load " + CONFIG_PATH, e);
        }
    }

    public ReplyKeyboard getKeyboard(String stageName) {
        return getKeyboard(stageName, Collections.emptyMap());
    }

    public ReplyKeyboard getKeyboard(UserFlowState state) {
        return getKeyboard(state.name(), Collections.emptyMap());
    }

    public ReplyKeyboard getKeyboard(UserFlowState state, Map<String, Object> params) {
        return getKeyboard(state.name(), params);
    }

    public ReplyKeyboard getKeyboard(String stageName, Map<String, Object> params) {
        KeyboardDefinition def = registry.get(stageName);
        if (def == null) {
            log.warn("No keyboard definition found for stage '{}'", stageName);
            return null;
        }

        return switch (def.getType()) {
            case INLINE -> buildInlineKeyboard(def, params);
            case REPLY -> buildReplyKeyboard(def, params);
            case REMOVE -> getRemoveKeyboard();
        };
    }

    public InlineKeyboardMarkup getInlineKeyboard(String stageName) {
        return getInlineKeyboard(stageName, Collections.emptyMap());
    }

    public InlineKeyboardMarkup getInlineKeyboard(String stageName, Map<String, Object> params) {
        ReplyKeyboard keyboard = getKeyboard(stageName, params);
        if (keyboard instanceof InlineKeyboardMarkup inline) {
            return inline;
        }
        log.warn("Keyboard '{}' is not of type INLINE", stageName);
        return null;
    }

    public ReplyKeyboardMarkup getReplyKeyboard(String stageName) {
        return getReplyKeyboard(stageName, Collections.emptyMap());
    }

    public ReplyKeyboardMarkup getReplyKeyboard(String stageName, Map<String, Object> params) {
        ReplyKeyboard keyboard = getKeyboard(stageName, params);
        if (keyboard instanceof ReplyKeyboardMarkup reply) {
            return reply;
        }
        log.warn("Keyboard '{}' is not of type REPLY", stageName);
        return null;
    }

    public ReplyKeyboardRemove getRemoveKeyboard() {
        return ReplyKeyboardRemove.builder()
                .removeKeyboard(true)
                .build();
    }

    private InlineKeyboardMarkup buildInlineKeyboard(KeyboardDefinition def, Map<String, Object> params) {
        List<InlineKeyboardRow> inlineRows = new ArrayList<>();

        for (List<ButtonConfig> row : def.getRows()) {
            List<InlineKeyboardButton> buttonsInRow = new ArrayList<>();
            for (ButtonConfig btn : row) {
                if (templateEngine.evaluateCondition(btn.getCondition(), params)) {
                    InlineKeyboardButton.InlineKeyboardButtonBuilder<?, ?> builder = InlineKeyboardButton.builder()
                            .text(templateEngine.evaluateText(btn.getText(), params));

                    if (btn.getCallbackData() != null) {
                        builder.callbackData(templateEngine.evaluateText(btn.getCallbackData(), params));
                    }
                    if (btn.getUrl() != null) {
                        builder.url(templateEngine.evaluateText(btn.getUrl(), params));
                    }

                    buttonsInRow.add(builder.build());
                }
            }

            if (!buttonsInRow.isEmpty()) {
                inlineRows.add(new InlineKeyboardRow(buttonsInRow));
            }
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(inlineRows)
                .build();
    }

    private ReplyKeyboardMarkup buildReplyKeyboard(KeyboardDefinition def, Map<String, Object> params) {
        List<KeyboardRow> replyRows = new ArrayList<>();

        for (List<ButtonConfig> row : def.getRows()) {
            List<KeyboardButton> buttonsInRow = new ArrayList<>();
            for (ButtonConfig btn : row) {
                if (templateEngine.evaluateCondition(btn.getCondition(), params)) {
                    buttonsInRow.add(new KeyboardButton(templateEngine.evaluateText(btn.getText(), params)));
                }
            }

            if (!buttonsInRow.isEmpty()) {
                replyRows.add(new KeyboardRow(buttonsInRow));
            }
        }

        return ReplyKeyboardMarkup.builder()
                .keyboard(replyRows)
                .resizeKeyboard(Boolean.TRUE.equals(def.getResizeKeyboard()))
                .oneTimeKeyboard(Boolean.TRUE.equals(def.getOneTimeKeyboard()))
                .build();
    }
}
