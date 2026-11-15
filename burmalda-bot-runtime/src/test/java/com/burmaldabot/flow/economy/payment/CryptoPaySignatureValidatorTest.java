package com.burmaldabot.flow.economy.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoPaySignatureValidatorTest {

    private CryptoPaySignatureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CryptoPaySignatureValidator();
    }

    @Test
    void testValidSignature() throws Exception {
        String token = "12345:TEST_TOKEN";
        String bodyString = "{\"update_id\":1,\"update_type\":\"invoice_paid\"}";
        byte[] bodyBytes = bodyString.getBytes(StandardCharsets.UTF_8);

        // Expected signature calculation:
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] secretKey = sha256.digest(token.getBytes(StandardCharsets.UTF_8));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] hmacBytes = mac.doFinal(bodyBytes);
        StringBuilder hex = new StringBuilder();
        for (byte b : hmacBytes) {
            hex.append(String.format("%02x", b));
        }
        String validSignature = hex.toString();

        assertTrue(validator.isValid(bodyBytes, validSignature, token));
    }

    @Test
    void testInvalidSignature() {
        String token = "12345:TEST_TOKEN";
        byte[] bodyBytes = "{\"test\":true}".getBytes(StandardCharsets.UTF_8);

        assertFalse(validator.isValid(bodyBytes, "invalid_hex_signature", token));
    }

    @Test
    void testNullAndEmptyInputs() {
        assertFalse(validator.isValid(null, "sig", "token"));
        assertFalse(validator.isValid(new byte[0], null, "token"));
        assertFalse(validator.isValid(new byte[0], "", "token"));
        assertFalse(validator.isValid(new byte[0], "sig", null));
        assertFalse(validator.isValid(new byte[0], "sig", ""));
    }
}
