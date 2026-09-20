package com.burmaldabot.input.impl;

import com.burmaldabot.config.AiRequestConfig;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.service.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;

import java.util.Comparator;
import java.util.Optional;

@Component
public class PhotoInputHandler extends PhotoInputHandlerBase {
    public PhotoInputHandler(TelegramFileService tfc, KieFileService kfc,
                             KieVideoService kvs, StorageFileService sfs,
                             AiTaskService ats, AiRequestConfig arc) {
        super(tfc, kfc, kvs, sfs, ats, arc);
    }

    @Override
    public boolean supports(Message message) {
        return message.hasPhoto();
    }

    @Override
    public Optional<String> handle(Message message, BotContext context) {
        PhotoSize largest = message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new IllegalStateException("No photos found"));
        String url = getTelegramPhotoLink(largest.getFileId(), "images/jpeg", context);
        requestVideo(url, context);
        return Optional.of("Give us a minute to handle your photo");
    }
}
