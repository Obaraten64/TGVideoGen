package com.burmaldabot.flow.economy.payment;

import com.burmaldabot.flow.economy.payment.dto.CreateInvoiceRequest;
import com.burmaldabot.flow.economy.payment.dto.CryptoInvoiceDto;
import com.burmaldabot.flow.economy.payment.dto.CryptoPayApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class CryptoPayClient {

    private final RestClient restClient;
    private final String apiToken;

    public CryptoPayClient(
            @Value("${crypto.pay.base-url:https://testnet-pay.crypt.bot/api}") String baseUrl,
            @Value("${crypto.pay.token:}") String apiToken) {
        this.apiToken = apiToken;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public CryptoInvoiceDto createInvoice(CreateInvoiceRequest request) {
        log.info("Calling Crypto Pay createInvoice for asset: {}, amount: {}", request.getAsset(), request.getAmount());

        CryptoPayApiResponse<CryptoInvoiceDto> response = restClient.post()
                .uri("/createInvoice")
                .header("Crypto-Pay-API-Token", apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.isOk() || response.getResult() == null) {
            String errorMsg = response != null ? String.valueOf(response.getError()) : "Empty response";
            log.error("Crypto Pay createInvoice returned failure: {}", errorMsg);
            throw new IllegalStateException("Failed to create Crypto Pay invoice: " + errorMsg);
        }

        return response.getResult();
    }

    public List<CryptoInvoiceDto> getInvoices(List<Long> invoiceIds) {
        String idsJoined = String.join(",", invoiceIds.stream().map(String::valueOf).toList());
        log.info("Calling Crypto Pay getInvoices for IDs: {}", idsJoined);

        CryptoPayApiResponse<List<CryptoInvoiceDto>> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getInvoices")
                        .queryParam("invoice_ids", idsJoined)
                        .build())
                .header("Crypto-Pay-API-Token", apiToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.isOk() || response.getResult() == null) {
            String errorMsg = response != null ? String.valueOf(response.getError()) : "Empty response";
            log.error("Crypto Pay getInvoices returned failure: {}", errorMsg);
            throw new IllegalStateException("Failed to get Crypto Pay invoices: " + errorMsg);
        }

        return response.getResult();
    }
}
