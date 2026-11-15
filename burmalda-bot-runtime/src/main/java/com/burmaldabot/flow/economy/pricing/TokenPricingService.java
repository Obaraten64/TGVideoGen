package com.burmaldabot.flow.economy.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenPricingService {

    private static final String EXTERNAL_CONFIG_FILE = "pricing.json";
    private static final String CLASSPATH_CONFIG_FILE = "pricing.json";
    private static final long RELOAD_CHECK_INTERVAL_MS = 5000L;

    private final ObjectMapper objectMapper;

    private final AtomicReference<PricingConfig> currentConfig = new AtomicReference<>(new PricingConfig());
    private final AtomicLong lastFileCheckTime = new AtomicLong(0L);
    private final AtomicLong lastModifiedTimestamp = new AtomicLong(0L);

    @PostConstruct
    public void init() {
        loadConfig();
    }

    public PricingConfig getConfig() {
        checkAndReloadIfModified();
        return currentConfig.get();
    }

    public BigDecimal calculatePrice(long tokens) {
        PricingConfig config = getConfig();
        BigDecimal baseTotal = config.getBasePricePerToken().multiply(BigDecimal.valueOf(tokens));

        // Check if matches any fast button discount or tier
        int discountPercent = 0;
        if (config.getFastButtons() != null) {
            for (FastButtonConfig btn : config.getFastButtons()) {
                if (btn.getTokens() == tokens) {
                    discountPercent = btn.getDiscountPercent();
                    break;
                }
            }
        }

        if (discountPercent > 0) {
            BigDecimal multiplier = BigDecimal.valueOf(100 - discountPercent)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            baseTotal = baseTotal.multiply(multiplier);
        }

        return baseTotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFastButtonPrice(FastButtonConfig btn) {
        PricingConfig config = getConfig();
        BigDecimal total = config.getBasePricePerToken().multiply(BigDecimal.valueOf(btn.getTokens()));
        if (btn.getDiscountPercent() > 0) {
            BigDecimal multiplier = BigDecimal.valueOf(100 - btn.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            total = total.multiply(multiplier);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void checkAndReloadIfModified() {
        long now = System.currentTimeMillis();
        if (now - lastFileCheckTime.get() < RELOAD_CHECK_INTERVAL_MS) {
            return;
        }
        lastFileCheckTime.set(now);

        Path externalPath = Paths.get(EXTERNAL_CONFIG_FILE);
        if (Files.exists(externalPath)) {
            try {
                long modified = Files.getLastModifiedTime(externalPath).toMillis();
                if (modified > lastModifiedTimestamp.get()) {
                    log.info("Detected change in {}, hot-reloading pricing configuration...", EXTERNAL_CONFIG_FILE);
                    loadFromExternalFile(externalPath.toFile());
                    lastModifiedTimestamp.set(modified);
                }
            } catch (Exception e) {
                log.warn("Failed to check last modified time for {}: {}", EXTERNAL_CONFIG_FILE, e.getMessage());
            }
        }
    }

    private synchronized void loadConfig() {
        Path externalPath = Paths.get(EXTERNAL_CONFIG_FILE);
        if (Files.exists(externalPath)) {
            loadFromExternalFile(externalPath.toFile());
            try {
                lastModifiedTimestamp.set(Files.getLastModifiedTime(externalPath).toMillis());
            } catch (Exception ignored) {
            }
            return;
        }

        loadFromClasspath();
    }

    private void loadFromExternalFile(File file) {
        try {
            PricingConfig config = objectMapper.readValue(file, PricingConfig.class);
            currentConfig.set(config);
            log.info("Loaded pricing configuration from external file: {}. Base price: {} {}",
                    file.getAbsolutePath(), config.getBasePricePerToken(), config.getAsset());
        } catch (Exception e) {
            log.error("Failed to parse external pricing config {}: {}. Falling back to classpath/default.",
                    file.getAbsolutePath(), e.getMessage());
            loadFromClasspath();
        }
    }

    private void loadFromClasspath() {
        try {
            ClassPathResource resource = new ClassPathResource(CLASSPATH_CONFIG_FILE);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    PricingConfig config = objectMapper.readValue(is, PricingConfig.class);
                    currentConfig.set(config);
                    log.info("Loaded pricing configuration from classpath: {}. Base price: {} {}",
                            CLASSPATH_CONFIG_FILE, config.getBasePricePerToken(), config.getAsset());
                }
            }
        } catch (Exception e) {
            log.error("Failed to load pricing config from classpath: {}. Using default values.", e.getMessage());
        }
    }
}
