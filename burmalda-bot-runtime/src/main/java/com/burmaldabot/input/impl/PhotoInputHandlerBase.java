package com.burmaldabot.input.impl;

import com.burmaldabot.config.AiRequestConfig;
import com.burmaldabot.exception.TelegramFileException;
import com.burmaldabot.input.TelegramInputHandlerBase;
import com.burmaldabot.model.ai.AiTaskContext;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.model.files.AllowedExtensions;
import com.burmaldabot.service.*;
import com.burmaldabot.model.files.TelegramFile;
import lombok.RequiredArgsConstructor;

import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
public abstract class PhotoInputHandlerBase implements TelegramInputHandlerBase {
    private static final String INPUT_FOLDER = "input";
    private final TelegramFileService fileService;
    private final KieFileService kieFileService;
    private final KieVideoService kieVideoService;
    private final StorageFileService storageFileService;
    private final AiTaskService aiTaskService;
    private final AiRequestConfig aiRequestConfig;

    public String getTelegramPhotoLink(String fileId, String mimeType, BotContext context) {
        TelegramFile telegramFile = fileService.downloadImage(fileId, mimeType, context);
        String path = storageFileService.generateFileName(INPUT_FOLDER, AllowedExtensions.JPG);
        storageFileService.saveFileLocally(telegramFile.content(), path);
        String encodedImage = Base64.getEncoder().encodeToString(telegramFile.content());
        return kieFileService.uploadBase64(encodedImage, fileId);
    }

    public void requestVideo(String url, BotContext context) {
        AiTaskContext aiTaskContext = new AiTaskContext(
                aiRequestConfig.getPrompt(),
                List.of(url),
                aiRequestConfig.getResolution(),
                aiRequestConfig.getDuration()
        );
        String taskId = kieVideoService.createImageToVideo(aiTaskContext);
        aiTaskService.createTask(taskId, context);
    }
}
