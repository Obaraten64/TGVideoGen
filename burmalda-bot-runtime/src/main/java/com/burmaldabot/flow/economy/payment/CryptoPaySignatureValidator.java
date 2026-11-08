package com.burmaldabot.flow.economy.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Slf4j
public class CryptoPaySignatureValidator {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SHA256 = "SHA-256";

    public boolean isValid(byte[] rawBody, String incomingSignature, String apiToken) {
        if (incomingSignature == null || incomingSignature.isBlank() || rawBody == null || apiToken == null || apiToken.isBlank()) {
            return false;
        }

        try {
            // 1. Secret key = SHA-256(apiToken)
            MessageDigest sha256 = MessageDigest.getInstance(SHA256);
            byte[] secretKey = sha256.digest(apiToken.getBytes(StandardCharsets.UTF_8));

            // 2. Compute HMAC-SHA256 of raw body
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secretKey, HMAC_SHA256));
            byte[] hmacBytes = mac.doFinal(rawBody);

            // 3. Hex encode
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                hexString.append(String.format("%02x", b));
            }
            String calculatedSignature = hexString.toString();

            // 4. Constant-time comparison
            return MessageDigest.isEqual(
                    calculatedSignature.getBytes(StandardCharsets.UTF_8),
                    incomingSignature.trim().getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Failed to calculate Crypto Pay HMAC-SHA256 signature", e);
            return false;
        }
    }
}
