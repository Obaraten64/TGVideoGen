package com.burmaldabot.flow.economy.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoInvoiceDto {

    @JsonProperty("invoice_id")
    private Long invoiceId;

    private String hash;

    @JsonProperty("currency_type")
    private String currencyType;

    private String asset;

    private String amount;

    @JsonProperty("paid_asset")
    private String paidAsset;

    @JsonProperty("paid_amount")
    private String paidAmount;

    @JsonProperty("fee_asset")
    private String feeAsset;

    @JsonProperty("fee_amount")
    private String feeAmount;

    @JsonProperty("pay_url")
    private String payUrl;

    @JsonProperty("bot_invoice_url")
    private String botInvoiceUrl;

    private String status;

    private String description;

    private String payload;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("paid_at")
    private String paidAt;
}
