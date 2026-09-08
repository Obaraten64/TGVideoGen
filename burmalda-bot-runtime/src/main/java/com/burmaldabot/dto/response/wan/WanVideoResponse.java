package com.burmaldabot.dto.response.wan;

public record WanVideoResponse(
        int code,
        String msg,
        WanTaskData data
) {
}
