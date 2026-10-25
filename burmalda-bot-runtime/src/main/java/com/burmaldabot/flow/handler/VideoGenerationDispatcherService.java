package com.burmaldabot.flow.handler;

import com.burmaldabot.config.AiRequestConfig;
import com.burmaldabot.flow.FlowResponseService;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.flow.economy.VideoPricing;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.flow.session.UserSessionService;
import com.burmaldabot.model.ai.AiTaskContext;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.model.files.AllowedExtensions;
import com.burmaldabot.model.files.TelegramFile;
import com.burmaldabot.service.*;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoGenerationDispatcherService {

    private static final String INPUT_FOLDER = "input";

    private final TelegramFileService fileService;
    private final KieFileService kieFileService;
    private final KieVideoService kieVideoService;
    private final StorageFileService storageFileService;
    private final AiTaskService aiTaskService;
    private final AiRequestConfig aiRequestConfig;
    private final UserBalanceService balanceService;
    private final UserSessionService sessionService;
    private final TelegramKeyboardFactory keyboardFactory;
    private final TelegramSender telegramSender;
    private final FlowResponseService responseService;

    public void startGeneration(BotContext context, BotUserSession session, String prompt) {
        String resolution = session.getSelectedResolution() != null ? session.getSelectedResolution() : "480p";
        int duration = session.getSelectedDuration() != null ? session.getSelectedDuration() : 3;
        boolean isFree = Boolean.TRUE.equals(session.getIsFreeTrial());

        long totalCost = VideoPricing.calculateTotalCost(resolution, duration, isFree);

        // Atomic Balance & Free Trial verification
        if (isFree) {
            boolean reserved = balanceService.tryConsumeFreeTrial(context.telegramUserId(), context.bot().getId());
            if (!reserved) {
                responseService.sendValidationError(
                        context,
                        "Бесплатная генерация уже использована. Пожалуйста, пополните баланс.",
                        keyboardFactory.createProfileKeyboard(false)
                );
                session.resetToMenu();
                sessionService.save(session);
                return;
            }
        } else {
            boolean deducted = balanceService.deductTokens(context.telegramUserId(), context.bot().getId(), totalCost);
            if (!deducted) {
                responseService.sendValidationError(
                        context,
                        "Недостаточно токенов на балансе для запуска генерации.",
                        keyboardFactory.createProfileKeyboard(false)
                );
                session.resetToMenu();
                sessionService.save(session);
                return;
            }
        }

        // Keyboard intentionally omitted: transient progress status message before final task submission confirmation
        telegramSender.sendMessage(
                context,
                "⏳ **Загружаем фото и подготавливаем генерацию...**",
                null
        );

        try {
            // Upload photos to Kie
            List<String> uploadedUrls = new ArrayList<>();
            for (String fileId : session.getPhotoFileIds()) {
                TelegramFile telegramFile = fileService.downloadImage(fileId, "image/jpeg", context);
                String path = storageFileService.generateFileName(INPUT_FOLDER, AllowedExtensions.JPG);
                storageFileService.saveFileLocally(telegramFile.content(), path);
                String encodedImage = Base64.getEncoder().encodeToString(telegramFile.content());
                String kieUrl = kieFileService.uploadBase64(encodedImage, fileId);
                uploadedUrls.add(kieUrl);
            }

            // Create task in Kie
            AiTaskContext aiTaskContext = new AiTaskContext(
                    prompt != null && !prompt.isBlank() ? prompt : aiRequestConfig.getPrompt(),
                    uploadedUrls,
                    resolution,
                    duration
            );

            String taskId = kieVideoService.createImageToVideo(aiTaskContext);
            aiTaskService.createTask(taskId, context);

            session.resetToMenu();
            sessionService.save(session);

            telegramSender.sendMessage(
                    context,
                    String.format(
                            """
                                    🚀 **Генерация запущена!**
                                    
                                    Параметры задачи:
                                    • Разрешение: **%s**
                                    • Длительность: **%d сек**
                                    • Фото: **%d шт**
                                    • Списано: **%d токенов**
                                    
                                    ⏳ Обычно создание видео занимает 1-3 минуты.
                                    Мы пришлем готовый результат прямо в этот диалог!""",
                            resolution,
                            duration,
                            uploadedUrls.size(),
                            totalCost
                    ),
                    keyboardFactory.createGenerationStatusKeyboard()
            );

        } catch (Exception e) {
            log.error("Error creating video generation task: {}", e.getMessage(), e);

            // Refund tokens if deduction occurred
            if (!isFree && totalCost > 0) {
                balanceService.addTokens(context.telegramUserId(), context.bot().getId(), totalCost);
            }

            // Keep user in current prompt step so their uploaded photos are not lost!
            responseService.sendValidationError(
                    context,
                    "Произошла ошибка при отправке задачи в нейросеть. Ваши фото сохранены, а токены возвращены на баланс.\n" +
                            "Попробуйте ввести другой промпт или выбрать стиль ниже:",
                    keyboardFactory.createPromptTemplatesKeyboard()
            );
        }
    }
}
