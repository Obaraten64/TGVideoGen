package com.burmaldabot.dto.response.callback;

public record KieCallbackData(
        String taskId,
        String model,
        String state,
        String param,
        String resultJson,
        String failCode,
        String failMsg,
        Long costTime,
        Long completeTime,
        Long createTime,
        Long updateTime,
        Integer creditsConsumed
) {
}
