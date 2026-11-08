package com.burmaldabot.controller;

import com.burmaldabot.flow.economy.payment.CryptoPaySignatureValidator;
import com.burmaldabot.flow.economy.payment.PaymentOrderService;
import com.burmaldabot.flow.economy.payment.dto.CryptoPayWebhookUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crypto-pay")
@RequiredArgsConstructor
@Slf4j
public class CryptoPayWebhookController {

    private final PaymentOrderService paymentOrderService;
    private final CryptoPaySignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    @Value("${crypto.pay.token:}")
    private String apiToken;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "crypto-pay-api-signature", required = false) String signature,
            @RequestBody byte[] rawBody) {

        if (signature == null || !signatureValidator.isValid(rawBody, signature, apiToken)) {
            log.warn("Crypto Pay webhook rejected: invalid or missing signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            CryptoPayWebhookUpdate update = objectMapper.readValue(rawBody, CryptoPayWebhookUpdate.class);
            log.info("Received Crypto Pay webhook: update_type={}, update_id={}", update.getUpdateType(), update.getUpdateId());

            if ("invoice_paid".equalsIgnoreCase(update.getUpdateType()) && update.getPayload() != null) {
                paymentOrderService.fulfillInvoice(update.getPayload());
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing Crypto Pay webhook payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing error");
        }
    }
}
