package com.burmaldabot.service;

import com.burmaldabot.dto.response.callback.KieCallback;
import com.burmaldabot.dto.response.callback.KieCallbackData;
import com.burmaldabot.dto.response.callback.KieResult;
import com.burmaldabot.exception.KieApiException;
import com.burmaldabot.model.files.AllowedExtensions;
import com.burmaldabot.telegram.TelegramSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KieCallbackService {

    private final ObjectMapper objectMapper;
    private final TelegramSender telegramSender;
    private final KieFileService kieFileService;
    private final StorageFileService storageFileService;
    private static final String OUTPUT_FOLDER = "output";

    public void handle(KieCallback callback, String chatId) {

        KieCallbackData data = callback.data();

        System.out.println("Kie task: " + data.taskId());
        System.out.println("State: " + data.state());

        if ("success".equalsIgnoreCase(data.state())) {

            try {
                KieResult result =
                        objectMapper.readValue(
                                data.resultJson(),
                                KieResult.class
                        );

                String videoUrl =
                        result.resultUrls().get(0);
                byte[] video = kieFileService.downloadVideo(videoUrl);
                String path = storageFileService.generateFileName(OUTPUT_FOLDER, AllowedExtensions.MP4);
                storageFileService.saveFileLocally(video, path);

                System.out.println(
                        "Generated video: " + videoUrl
                );

                telegramSender.sendDocument(Long.parseLong(chatId), video, "generate-video.mp4");

            } catch (JsonProcessingException | NumberFormatException e) {
                throw new KieApiException(
                        "Failed to parse Kie resultJson",
                        e
                );
            }
        }

        if ("fail".equalsIgnoreCase(data.state())) {

            System.err.println(
                    "Kie generation failed: "
                            + data.failCode()
                            + " - "
                            + data.failMsg()
            );
        }
    }
}
