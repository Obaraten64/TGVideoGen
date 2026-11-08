package com.burmaldabot.flow.economy.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateInvoiceRequest {

    private String asset;

    private String amount;

    private String description;

    private String payload;

    @JsonProperty("paid_btn_name")
    private String paidBtnName;

    @JsonProperty("paid_btn_url")
    private String paidBtnUrl;

    @JsonProperty("expires_in")
    private Integer expiresIn;
}
