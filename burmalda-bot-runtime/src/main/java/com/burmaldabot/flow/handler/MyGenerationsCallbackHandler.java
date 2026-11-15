package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.UserFlowState;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.ai.AiTask;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.repository.AiTaskRepository;
import com.burmaldabot.service.MessageService;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class MyGenerationsCallbackHandler implements CallbackQueryHandler {

    private final AiTaskRepository aiTaskRepository;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final MessageService messageService;

    @Override
    public boolean supports(String callbackData) {
        return "profile:my_generations".equals(callbackData);
    }

    @Override
    public Set<UserFlowState> getAllowedStates() {
        return Set.of(); // Allowed globally
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        List<AiTask> recentTasks = aiTaskRepository.findTop5ByBotIdAndTelegramUserIdOrderByCreatedAtDesc(
                context.bot().getId(),
                context.telegramUserId()
        );

        if (recentTasks.isEmpty()) {
            telegramSender.sendMessage(
                    context,
                    messageService.getMessage("generations.empty"),
                    keyboardFactory.createMainMenu()
            );
            return;
        }

        StringBuilder sb = new StringBuilder(messageService.getMessage("generations.header"));
        for (int i = 0; i < recentTasks.size(); i++) {
            AiTask task = recentTasks.get(i);
            String statusEmoji = switch (task.getStatus()) {
                case SUCCESS -> messageService.getMessage("generations.status.success");
                case PROCESSING -> messageService.getMessage("generations.status.processing");
                case FAILED -> messageService.getMessage("generations.status.failed");
                default -> messageService.getMessage("generations.status.created");
            };
            sb.append(messageService.getMessage(
                    "generations.item",
                    i + 1,
                    task.getAiTaskId().substring(0, Math.min(8, task.getAiTaskId().length())),
                    statusEmoji
            ));
        }

        telegramSender.sendMessage(
                context,
                sb.toString(),
                keyboardFactory.createMainMenu()
        );
    }
}
