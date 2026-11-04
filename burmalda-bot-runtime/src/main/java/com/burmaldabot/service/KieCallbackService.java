package com.burmaldabot.service;

import com.burmaldabot.dto.response.callback.KieCallback;
import com.burmaldabot.dto.response.callback.KieCallbackData;
import com.burmaldabot.dto.response.callback.KieResult;
import com.burmaldabot.exception.KieApiException;
import com.burmaldabot.flow.economy.UserBalanceService;
import com.burmaldabot.model.ai.AiTask;
import com.burmaldabot.model.ai.AiTaskStatus;
import com.burmaldabot.model.bot.Bot;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.model.files.AllowedExtensions;
import com.burmaldabot.repository.AiTaskRepository;
import com.burmaldabot.repository.BotRepository;
import com.burmaldabot.telegram.TelegramSender;
import com.burmaldabot.telegram.keyboard.TelegramKeyboardFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class KieCallbackService {
    private static final String OUTPUT_FOLDER = "output";
    private final ObjectMapper objectMapper;
    private final TelegramSender telegramSender;
    private final KieFileService kieFileService;
    private final StorageFileService storageFileService;
    private final AiTaskRepository aiTaskRepository;
    private final BotRepository botRepository;
    private final UserBalanceService balanceService;
    private final TelegramKeyboardFactory keyboardFactory;

    public void handle(KieCallback callback) {
        KieCallbackData data = callback.data();
        AiTask task = aiTaskRepository.findByAiTaskId(data.taskId())
                .orElseThrow(() -> new KieApiException("AiTask not found for taskId: " + data.taskId()));
        Bot bot = botRepository.findById(task.getBotId())
                .orElseThrow(() -> new KieApiException("Bot not found for botId: " + task.getBotId()));
        BotContext botContext = new BotContext(bot, task.getChatId(), task.getTelegramUserId());

        log.info("Kie task: {}", data.taskId());
        log.info("State: {}", data.state());

        if ("success".equalsIgnoreCase(data.state())) {
            try {
                KieResult result = objectMapper.readValue(data.resultJson(), KieResult.class);
                String videoUrl = result.resultUrls().get(0);
                byte[] video = kieFileService.downloadVideo(videoUrl);
                String path = storageFileService.generateFileName(OUTPUT_FOLDER, AllowedExtensions.MP4);
                storageFileService.saveFileLocally(video, path);

                log.info("Generated video downloaded: {}", videoUrl);

                telegramSender.sendVideo(
                        botContext,
                        video,
                        "generated-video.mp4",
                        "🎬 **Ваше видео готово!**"
                );

                telegramSender.sendMessage(
                        botContext,
                        "Что делаем дальше?",
                        keyboardFactory.createGenerationStatusKeyboard()
                );

                task.setStatus(AiTaskStatus.SUCCESS);
                task.setCompletedAt(Instant.now());
                aiTaskRepository.save(task);

            } catch (JsonProcessingException | NumberFormatException e) {
                task.setStatus(AiTaskStatus.FAILED);
                task.setCompletedAt(Instant.now());
                aiTaskRepository.save(task);
                throw new KieApiException("Failed to parse Kie resultJson", e);
            }
        }

        if ("fail".equalsIgnoreCase(data.state())) {
            log.warn("Kie generation failed: {} - {}", data.failCode(), data.failMsg());
            task.setStatus(AiTaskStatus.FAILED);
            task.setCompletedAt(Instant.now());
            aiTaskRepository.save(task);

            telegramSender.sendMessage(
                    botContext,
                    "❌ К сожалению, при генерации видео произошла ошибка на стороне нейросети. Попробуйте еще раз с другим фото или промптом.",
                    keyboardFactory.createMainMenu()
            );
        }
    }
}
