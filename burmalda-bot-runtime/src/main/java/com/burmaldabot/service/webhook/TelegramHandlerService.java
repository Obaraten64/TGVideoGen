package com.burmaldabot.service.webhook;

import com.burmaldabot.input.TelegramInputDispatcher;
import com.burmaldabot.model.bot.Bot;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TelegramHandlerService {
    private final TelegramInputDispatcher telegramInputDispatcher;
    private final TelegramSender telegramSender;

    public void consume(Update update, Bot bot) {
        Message message = update.getMessage();
        if (message == null || !update.hasMessage()) {
            return;
        }

        BotContext context = new BotContext(
                bot,
                message.getChatId(),
                message.getFrom().getId()
        );
        Optional<String> optionalResponse = telegramInputDispatcher.dispatch(message, context);
        if (optionalResponse.isEmpty() || optionalResponse.get().isEmpty()) {
            return;
        }

        String responseMessage = optionalResponse.get();
        telegramSender.sendMessage(responseMessage, context);
    }
}
