package com.burmaldabot.flow.economy.payment;

import com.burmaldabot.flow.economy.payment.dto.CryptoInvoiceDto;
import com.burmaldabot.flow.economy.payment.dto.CryptoInvoicesResultDto;
import com.burmaldabot.flow.economy.payment.dto.CryptoPayApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoPayClientDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetInvoicesDeserialization() throws Exception {
        // Real-world response payload from Crypto Pay API /getInvoices
        String json = """
        {
          "ok": true,
          "result": {
            "count": 1,
            "items": [
              {
                "invoice_id": 149258,
                "hash": "B1a2C3d4E5f6",
                "currency_type": "crypto",
                "asset": "USDT",
                "amount": "15.5",
                "pay_url": "https://t.me/CryptoTestnetBot?start=IV1a2C3d4E5f6",
                "bot_invoice_url": "https://t.me/CryptoTestnetBot?start=IV1a2C3d4E5f6",
                "description": "Subscription for Pro Plan",
                "status": "paid",
                "created_at": "2026-06-06T14:25:00Z",
                "paid_at": "2026-06-06T14:29:55Z",
                "paid_amount": "15.5",
                "paid_usd_rate": "1.0",
                "fee": "0.0775",
                "fee_asset": "USDT",
                "payload": "order-uuid-12345"
              }
            ]
          }
        }
        """;

        CryptoPayApiResponse<CryptoInvoicesResultDto> response = objectMapper.readValue(
                json, new TypeReference<>() {}
        );

        assertTrue(response.isOk());
        assertNotNull(response.getResult());
        assertEquals(1, response.getResult().getCount());
        assertNotNull(response.getResult().getItems());
        assertEquals(1, response.getResult().getItems().size());

        CryptoInvoiceDto invoice = response.getResult().getItems().get(0);
        assertEquals(149258L, invoice.getInvoiceId());
        assertEquals("paid", invoice.getStatus());
        assertEquals("USDT", invoice.getAsset());
        assertEquals("15.5", invoice.getAmount());
        assertEquals("order-uuid-12345", invoice.getPayload());
        assertEquals("0.0775", invoice.getFeeAmount());
    }

    @Test
    void testCreateInvoiceDeserialization() throws Exception {
        String json = """
        {
          "ok": true,
          "result": {
            "invoice_id": 9999,
            "hash": "TestHash",
            "currency_type": "crypto",
            "asset": "USDT",
            "amount": "5.0",
            "bot_invoice_url": "https://t.me/CryptoBot?start=IVtest",
            "status": "active",
            "created_at": "2026-11-15T00:00:00Z"
          }
        }
        """;

        CryptoPayApiResponse<CryptoInvoiceDto> response = objectMapper.readValue(
                json, new TypeReference<>() {}
        );

        assertTrue(response.isOk());
        assertNotNull(response.getResult());
        assertEquals(9999L, response.getResult().getInvoiceId());
        assertEquals("active", response.getResult().getStatus());
        assertEquals("https://t.me/CryptoBot?start=IVtest", response.getResult().getBotInvoiceUrl());
    }
}
