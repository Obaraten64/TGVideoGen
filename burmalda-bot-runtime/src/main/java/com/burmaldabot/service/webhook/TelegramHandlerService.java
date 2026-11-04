package com.burmaldabot.service.webhook;

import com.burmaldabot.flow.UserFlowDispatcher;
import com.burmaldabot.model.bot.Bot;
import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramHandlerService {

    private final UserFlowDispatcher userFlowDispatcher;

    public void consume(Update update, Bot bot) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            Long chatId = callback.getMessage().getChatId();
            Long userId = callback.getFrom().getId();

            BotContext context = new BotContext(bot, chatId, userId);
            userFlowDispatcher.dispatchCallback(context, callback);
            return;
        }

        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            Long userId = message.getFrom() != null ? message.getFrom().getId() : chatId;

            BotContext context = new BotContext(bot, chatId, userId);
            userFlowDispatcher.dispatchMessage(context, message);
        }
    }
}
