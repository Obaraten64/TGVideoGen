package com.burmaldabot.dto.response.wan;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WanTaskData(
        String taskId
) {
}
