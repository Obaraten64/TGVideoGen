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
public class CryptoPayWebhookUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("update_type")
    private String updateType;

    @JsonProperty("request_date")
    private String requestDate;

    private CryptoInvoiceDto payload;
}
