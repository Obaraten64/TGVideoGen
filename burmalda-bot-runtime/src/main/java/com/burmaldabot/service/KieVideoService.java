package com.burmaldabot.service;

import com.burmaldabot.dto.request.wan.WanVideoInput;
import com.burmaldabot.dto.request.wan.WanVideoRequest;
import com.burmaldabot.dto.response.wan.WanVideoResponse;
import com.burmaldabot.exception.KieApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KieVideoService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    @Value("${kie.callback-url}")
    private final String callbackUrl;
    @Value("${kie.api.ai.base-url}")
    private final String aiApiBaseUrl;
    @Value("${kie.api.key}")
    private final String aiApiKey;

    public String createImageToVideo(
            String imageUrl,
            String prompt,
            String chatId
    ) {
        WanVideoInput input = new WanVideoInput(
                prompt,
                List.of(imageUrl),
                "480P",
                "adaptive",
                10,
                true,
                false
        );

        WanVideoRequest request = new WanVideoRequest(
                "wan/3-0-video",
                callbackUrl + chatId,
                input
        );

        try {
            String requestJson =
                    objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(
                            aiApiBaseUrl
                                    + "/api/v1/jobs/createTask"
                    ))
                    .timeout(Duration.ofSeconds(30))
                    .header(
                            "Authorization",
                            "Bearer " + aiApiKey
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers
                                    .ofString(requestJson)
                    )
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                throw new KieApiException(
                        "Kie.ai returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            WanVideoResponse result =
                    objectMapper.readValue(
                            response.body(),
                            WanVideoResponse.class
                    );

            if (result.code() != 200) {
                throw new KieApiException(
                        "Kie.ai task creation failed: "
                                + result.msg()
                );
            }

            return result.data().taskId();

        } catch (IOException e) {
            throw new KieApiException(
                    "Failed to communicate with Kie.ai",
                    e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new KieApiException(
                    "Kie.ai request interrupted",
                    e
            );
        }
    }
}
