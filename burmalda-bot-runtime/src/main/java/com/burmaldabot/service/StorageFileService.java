package com.burmaldabot.service;

import com.burmaldabot.model.files.AllowedExtensions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageFileService {
    public void saveFileLocally(byte[] file, String pathString) {
        try {
            Path path = Paths.get(pathString);
            log.info("File saved to {}", path.toAbsolutePath());

            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.write(path, file);
        } catch (IOException e) {
            log.error("Failed to save file locally", e);
        }
    }

    public String generateFileName(String relativePath, AllowedExtensions extension) {
        return relativePath + "/generated-" + System.currentTimeMillis() + "." + extension.getExtension();
    }
}
