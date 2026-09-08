package com.burmaldabot.model.files;

public record TelegramFile(String fileId,
                           String filePath,
                           byte[] content) {
}
