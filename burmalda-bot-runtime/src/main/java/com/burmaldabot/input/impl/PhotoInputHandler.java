package com.burmaldabot.input.impl;

import com.burmaldabot.service.StorageFileService;
import com.burmaldabot.service.TelegramFileService;
import com.burmaldabot.service.KieFileService;
import com.burmaldabot.service.KieVideoService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;

import java.util.Comparator;
import java.util.Optional;

@Component
public class PhotoInputHandler extends PhotoInputHandlerBase {
    public PhotoInputHandler(TelegramFileService tfc, KieFileService kfc,
                             KieVideoService kvs, StorageFileService sfs) {
        super(tfc, kfc, kvs, sfs);
    }

    @Override
    public boolean supports(Message message) {
        return message.hasPhoto();
    }

    @Override
    public Optional<String> handle(Message message) {
        PhotoSize largest = message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new IllegalStateException("No photos found"));
        String url = getTelegramPhotoLink(largest.getFileId(), "images/jpeg");
        requestVideo(url, message.getChatId().toString());
        return Optional.of("Give us a minute to handle your photo");
    }
}
