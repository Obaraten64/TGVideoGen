package com.burmaldabot.flow.economy.pricing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PricingConfig {
    @Builder.Default
    private String asset = "USDT";

    @Builder.Default
    private BigDecimal basePricePerToken = new BigDecimal("0.01");

    @Builder.Default
    private int minTokens = 10;

    @Builder.Default
    private int maxTokens = 50000;

    @Builder.Default
    private int invoiceExpiresInSeconds = 1800;

    @Builder.Default
    private List<FastButtonConfig> fastButtons = new ArrayList<>();
}
