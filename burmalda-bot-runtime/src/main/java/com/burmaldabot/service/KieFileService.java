package com.burmaldabot.service;

import com.burmaldabot.dto.request.KieBase64UploadRequest;
import com.burmaldabot.dto.response.KieBase64UploadResponse;
import com.burmaldabot.exception.KieApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class KieFileService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Value("${kie.api.storage.base-url}")
    private final String baseUrl;
    @Value("${kie.api.key}")
    private final String apiKey;

    public String uploadBase64(
            String dataUrl,
            String fileName) {

        KieBase64UploadRequest request =
                new KieBase64UploadRequest(
                        dataUrl,
                        "telegram-images",
                        fileName
                );

        try {
            String json = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(
                            baseUrl + "/api/file-base64-upload"
                    ))
                    .header(
                            "Authorization",
                            "Bearer " + apiKey
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                throw new KieApiException(
                        "Kie.ai upload failed: HTTP "
                                + response.statusCode()
                                + " - "
                                + response.body()
                );
            }

            KieBase64UploadResponse result =
                    objectMapper.readValue(
                            response.body(),
                            KieBase64UploadResponse.class
                    );

            if (!result.success()) {
                throw new KieApiException(
                        "Kie.ai upload failed: "
                                + result.msg()
                );
            }

            return result.data().downloadUrl();

        } catch (IOException e) {
            throw new KieApiException(
                    "Failed to communicate with Kie.ai",
                    e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new KieApiException(
                    "Kie.ai request was interrupted",
                    e
            );
        }
    }

    public byte[] downloadVideo(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to download file. HTTP status: "
                                + response.statusCode()
                );
            }

            return response.body();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to download file: " + url, e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "File download interrupted", e
            );
        }
    }
}
