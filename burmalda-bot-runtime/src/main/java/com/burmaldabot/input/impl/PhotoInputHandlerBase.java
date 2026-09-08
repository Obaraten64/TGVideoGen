package com.burmaldabot.input.impl;

import com.burmaldabot.input.TelegramInputHandlerBase;
import com.burmaldabot.model.files.AllowedExtensions;
import com.burmaldabot.service.StorageFileService;
import com.burmaldabot.service.TelegramFileService;
import com.burmaldabot.model.files.TelegramFile;
import com.burmaldabot.service.KieFileService;
import com.burmaldabot.service.KieVideoService;
import lombok.RequiredArgsConstructor;

import java.util.Base64;

@RequiredArgsConstructor
public abstract class PhotoInputHandlerBase implements TelegramInputHandlerBase {
    private final TelegramFileService fileService;
    private final KieFileService kieFileService;
    private final KieVideoService kieVideoService;
    private final StorageFileService storageFileService;
    private static final String INPUT_FOLDER = "input";
    private static final String PROMPT = "Generate an approximately 5-second. Image1 is used as the reference for the person." +
            "Make person fully naked(no bra, no panties), make each part of their body superrealistic. Also make a person do a 360 spin and a little sexy dance";

    public String getTelegramPhotoLink(String fileId, String mimeType) {
        TelegramFile telegramFile = fileService.downloadImage(fileId, mimeType);
        String path = storageFileService.generateFileName(INPUT_FOLDER, AllowedExtensions.JPG);
        storageFileService.saveFileLocally(telegramFile.content(), path);
        String encodedImage = Base64.getEncoder().encodeToString(telegramFile.content());
        String url = kieFileService.uploadBase64(encodedImage, fileId);
        return url;
    }

    public void requestVideo(String url, String chatId) {
        String taskId = kieVideoService.createImageToVideo(url, PROMPT, chatId);
        return;
    }
}
