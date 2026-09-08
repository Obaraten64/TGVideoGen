package com.burmaldabot.dto.response;

public record KieBase64UploadResponse(
        boolean success,
        int code,
        String msg,
        UploadedFile data
) {

    public record UploadedFile(
            boolean success,
            String fileName,
            String filePath,
            String downloadUrl,
            Long fileSize,
            String mimeType,
            String uploadedAt
    ) {}
}
