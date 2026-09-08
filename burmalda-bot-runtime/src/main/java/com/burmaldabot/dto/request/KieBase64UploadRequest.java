package com.burmaldabot.dto.request;

public record KieBase64UploadRequest(
        String base64Data,
        String uploadPath,
        String fileName
) {}
