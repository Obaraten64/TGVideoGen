package com.burmaldabot.flow;

import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowResponseService {

    private final TelegramSender telegramSender;

    /**
     * Sends standardized validation error with fallback keyboard to ensure the user is not left stranded.
     */
    public void sendValidationError(BotContext context, String errorMessage, ReplyKeyboard fallbackKeyboard) {
        String formatted = errorMessage.startsWith("⚠️") ? errorMessage : "⚠️ " + errorMessage;
        telegramSender.sendMessage(context, formatted, fallbackKeyboard);
    }

    /**
     * Alerts the user that they clicked a button belonging to a previous or obsolete menu.
     */
    public void sendStaleAlert(BotContext context, String callbackQueryId) {
        telegramSender.answerCallbackQuery(
                context,
                callbackQueryId,
                "⚠️ Это действие устарело. Пожалуйста, используйте актуальное меню внизу.",
                true
        );
    }

    /**
     * Clears inline buttons on an obsolete message and optionally updates the text to show confirmed choice.
     */
    public void clearInlineMarkup(BotContext context, Integer messageId, String updatedText) {
        if (messageId != null) {
            telegramSender.editMessageText(context, messageId, updatedText, null);
        }
    }
}
