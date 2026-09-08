package com.burmaldabot.dto.response.callback;

public record KieCallback(
        int code,
        String msg,
        KieCallbackData data
) {
}
