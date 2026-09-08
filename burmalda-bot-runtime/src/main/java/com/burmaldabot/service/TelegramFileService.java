package com.burmaldabot.service;

import com.burmaldabot.exception.TelegramFileException;
import com.burmaldabot.model.files.TelegramFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class TelegramFileService {
    private final String telegramToken;
    private final TelegramClient telegramClient;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TelegramFile downloadImage(String fileId, String mimeType) throws TelegramFileException {
        byte[] content = download(fileId);
        if (!isAnActualImage(content)) {
            throw new TelegramFileException(
                    "Telegram file is not an image: " + fileId
            );
        }
        return new TelegramFile(fileId, mimeType, content);
    }

    private byte[] download(String fileId) {
        try {
            String url = resolveTelegramPath(fileId);
            HttpResponse<byte[]> response = downloadTelegramFile(url);
            return response.body();
        } catch (TelegramApiException | IOException |
                 InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new TelegramFileException(
                    "Failed to download Telegram file: " + fileId,
                    e
            );
        }
    }

    private boolean isAnActualImage(byte[] content) {
        try (ByteArrayInputStream input =
                     new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(input);
            if (image != null) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private String buildDownloadUrl(String filePath) {
        return "https://api.telegram.org/file/bot"
                + telegramToken
                + "/"
                + filePath;
    }

    private String resolveTelegramPath(String fileId) throws TelegramApiException {
        File file = telegramClient.execute(
                GetFile.builder()
                        .fileId(fileId)
                        .build()
        );

        if (file.getFilePath() == null) {
            throw new TelegramFileException(
                    "Telegram returned no file path: " + fileId
            );
        }

        return buildDownloadUrl(file.getFilePath());
    }

    private HttpResponse<byte[]> downloadTelegramFile(String url) throws IOException,
            InterruptedException, IllegalArgumentException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() != 200) {
            throw new TelegramFileException(
                    "Failed to download Telegram file. HTTP status: "
                            + response.statusCode()
            );
        }

        return response;
    }
}
