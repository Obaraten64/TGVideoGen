package com.burmaldabot.input.impl;

import com.burmaldabot.config.AiRequestConfig;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.service.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

@Component
public class PhotoDocumentInputHandler extends PhotoInputHandlerBase {
    public PhotoDocumentInputHandler(TelegramFileService tfc, KieFileService kfc,
                                     KieVideoService kvs, StorageFileService sfs,
                                     AiTaskService ats, AiRequestConfig arc) {
        super(tfc, kfc, kvs, sfs, ats, arc);
    }

    @Override
    public boolean supports(Message message) {
        return message.hasDocument() && isImage(message.getDocument());
    }

    @Override
    public Optional<String> handle(Message message, BotContext context) {
        Document document = message.getDocument();
        String url = getTelegramPhotoLink(document.getFileId(), document.getMimeType(), context);
        requestVideo(url, context);
        return  Optional.of("Give us a minute to handle your photo");
    }

    private boolean isImage(Document document) {
        return document.getMimeType() != null
                && document.getMimeType().startsWith("image/");
    }
}
