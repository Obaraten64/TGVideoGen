package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainMenuCallbackHandler implements CallbackQueryHandler {

    private final UserSessionService sessionService;
    private final UserBalanceService balanceService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("menu:");
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        switch (data) {
            case "menu:generate" -> {
                session.startPaidGeneration();
                sessionService.save(session);
                telegramSender.sendMessage(
                        context,
                        "🎬 **Шаг 1 из 3: Выберите качество видео**\n\n" +
                                "Чем выше качество, тем больше токенов потребуется на генерацию:\n" +
                                "• 480p — 14 токенов/сек\n" +
                                "• 720p — 28 токенов/сек\n" +
                                "• 1080p — 56 токенов/сек",
                        keyboardFactory.createResolutionKeyboard()
                );
            }
            case "menu:free_trial" -> {
                boolean used = balanceService.hasUsedFreeTrial(context.telegramUserId(), context.bot().getId());
                if (used) {
                    telegramSender.sendMessage(
                            context,
                            "⚠️ **Бесплатная генерация уже использована!**\n\n" +
                                    "Вы можете пополнить баланс в профиле и создавать видео в любом разрешении.",
                            keyboardFactory.createProfileKeyboard(false)
                    );
                    return;
                }

                session.startFreeTrial();
                sessionService.save(session);
                telegramSender.sendMessage(
                        context,
                        "🎁 **Бесплатная пробная генерация активирована!**\n\n" +
                                "Параметры: **480p, 3 секунды** (0 токенов).\n\n" +
                                "📸 **Отправьте фото (до 10 шт)**. Отправляйте по одному или альбомом.\n" +
                                "После отправки нажмите кнопку «Подтвердить фото» под строкой ввода.",
                        keyboardFactory.createPhotoCollectionReplyKeyboard(0)
                );
            }
            case "menu:profile" -> {
                long balance = balanceService.getBalance(context.telegramUserId(), context.bot().getId());
                boolean freeTrialAvailable = !balanceService.hasUsedFreeTrial(context.telegramUserId(), context.bot().getId());

                String profileText = String.format(
                        "👤 **Ваш профиль**\n\n" +
                                "🆔 ID: `%d`\n" +
                                "💰 Баланс: **%d токенов**\n" +
                                "🎁 Бесплатная генерация: **%s**",
                        context.telegramUserId(),
                        balance,
                        freeTrialAvailable ? "Доступна ✅" : "Использована ❌"
                );

                telegramSender.sendMessage(
                        context,
                        profileText,
                        keyboardFactory.createProfileKeyboard(freeTrialAvailable)
                );
            }
            case "menu:characters" -> telegramSender.sendMessage(
                    context,
                    "🎭 **Персонажи**\n\n" +
                            "Каталог готовых персонажей и стилей в разработке.\n" +
                            "Скоро здесь появятся шаблоны для быстрого старта!",
                    keyboardFactory.createMainMenu()
            );
            case "menu:main" -> {
                session.resetToMenu();
                sessionService.save(session);
                telegramSender.sendMessage(
                        context,
                        "👋 **Главное меню Burmalda AI Video**\n\nВыберите действие ниже:",
                        keyboardFactory.createMainMenu()
                );
            }
            default -> log.warn("Unknown menu callback data: {}", data);
        }
    }
}
