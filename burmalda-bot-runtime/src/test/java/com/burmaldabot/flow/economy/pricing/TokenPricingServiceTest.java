package com.burmaldabot.flow.economy.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenPricingServiceTest {

    private TokenPricingService pricingService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        pricingService = new TokenPricingService(objectMapper);
        pricingService.init();
    }

    @Test
    void testBasePriceCalculation() {
        // 250 tokens at 0.01 = 2.50
        BigDecimal price = pricingService.calculatePrice(250);
        assertEquals(new BigDecimal("2.50"), price);
    }

    @Test
    void testFastButtonWithDiscount() {
        // Fast button 500 tokens has 10% discount: 500 * 0.01 * 0.90 = 4.50
        BigDecimal price500 = pricingService.calculatePrice(500);
        assertEquals(new BigDecimal("4.50"), price500);

        // Fast button 1500 tokens has 20% discount: 1500 * 0.01 * 0.80 = 12.00
        BigDecimal price1500 = pricingService.calculatePrice(1500);
        assertEquals(new BigDecimal("12.00"), price1500);

        // Fast button 100 tokens has 0% discount: 100 * 0.01 = 1.00
        BigDecimal price100 = pricingService.calculatePrice(100);
        assertEquals(new BigDecimal("1.00"), price100);
    }

    @Test
    void testCustomFastButtonCalculation() {
        FastButtonConfig btn = new FastButtonConfig(300, 15);
        // 300 * 0.01 * 0.85 = 2.55
        BigDecimal price = pricingService.calculateFastButtonPrice(btn);
        assertEquals(new BigDecimal("2.55"), price);
    }
}
