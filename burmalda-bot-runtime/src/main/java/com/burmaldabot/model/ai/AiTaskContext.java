package com.burmaldabot.model.ai;

import java.util.List;

public record AiTaskContext(
        String prompt,
        List<String> url,
        String resolution,
        Integer duration
) {
}
