package com.burmaldabot.input.impl;

import com.burmaldabot.service.StorageFileService;
import com.burmaldabot.service.TelegramFileService;
import com.burmaldabot.service.KieFileService;
import com.burmaldabot.service.KieVideoService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
public class PhotoDocumentInputHandler extends PhotoInputHandlerBase {
    public PhotoDocumentInputHandler(TelegramFileService tfc, KieFileService kfc,
                                     KieVideoService kvs, StorageFileService sfs) {
        super(tfc, kfc, kvs, sfs);
    }

    @Override
    public boolean supports(Message message) {
        return message.hasDocument() && isImage(message.getDocument());
    }

    @Override
    public Optional<String> handle(Message message) {
        Document document = message.getDocument();
        String url = getTelegramPhotoLink(document.getFileId(), document.getMimeType());
        requestVideo(url, message.getChatId().toString());
        return  Optional.of("Give us a minute to handle your photo");
    }

    private boolean isImage(Document document) {
        return document.getMimeType() != null
                && document.getMimeType().startsWith("image/");
    }
}
