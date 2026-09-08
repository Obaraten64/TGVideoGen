package com.burmaldabot.dto.request.wan;

public record WanVideoRequest(
        String model,
        String callBackUrl,
        WanVideoInput input
) {
}
