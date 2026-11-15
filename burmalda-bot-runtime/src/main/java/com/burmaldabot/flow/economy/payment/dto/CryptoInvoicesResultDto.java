package com.burmaldabot.flow.economy.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoInvoicesResultDto {

    @Builder.Default
    private List<CryptoInvoiceDto> items = new ArrayList<>();

    private Integer count;
}
