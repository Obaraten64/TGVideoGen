package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.ai.AiTask;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.repository.AiTaskRepository;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileCallbackHandler implements CallbackQueryHandler {

    private final UserBalanceService balanceService;
    private final AiTaskRepository aiTaskRepository;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("profile:");
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        switch (data) {
            case "profile:topup" -> {
                // In a production system, this generates an invoice or payment link.
                // For demo/development, we credit 100 test tokens.
                balanceService.addTokens(context.telegramUserId(), context.bot().getId(), 100L);
                long newBalance = balanceService.getBalance(context.telegramUserId(), context.bot().getId());

                telegramSender.sendMessage(
                        context,
                        String.format(
                                "💳 **Баланс пополнен!**\n\n" +
                                        "Вам начислено +100 тестовых токенов.\n" +
                                        "Текущий баланс: **%d токенов**",
                                newBalance
                        ),
                        keyboardFactory.createProfileKeyboard(!balanceService.hasUsedFreeTrial(context.telegramUserId(), context.bot().getId()))
                );
            }
            case "profile:my_generations" -> {
                List<AiTask> recentTasks = aiTaskRepository.findTop5ByBotIdAndTelegramUserIdOrderByCreatedAtDesc(
                        context.bot().getId(),
                        context.telegramUserId()
                );

                if (recentTasks.isEmpty()) {
                    telegramSender.sendMessage(
                            context,
                            "🎞 **Мои генерации**\n\nУ вас пока нет созданных задач на генерацию видео.",
                            keyboardFactory.createMainMenu()
                    );
                    return;
                }

                StringBuilder sb = new StringBuilder("🎞 **Ваши последние генерации:**\n\n");
                for (int i = 0; i < recentTasks.size(); i++) {
                    AiTask task = recentTasks.get(i);
                    String statusEmoji = switch (task.getStatus()) {
                        case SUCCESS -> "✅ Готово";
                        case PROCESSING -> "⏳ В обработке";
                        case FAILED -> "❌ Ошибка";
                        default -> "🕒 Создана";
                    };
                    sb.append(String.format("%d. Задача `#%s` — %s\n", i + 1, task.getAiTaskId().substring(0, Math.min(8, task.getAiTaskId().length())), statusEmoji));
                }

                telegramSender.sendMessage(
                        context,
                        sb.toString(),
                        keyboardFactory.createMainMenu()
                );
            }
            default -> log.warn("Unknown profile callback: {}", data);
        }
    }
}
