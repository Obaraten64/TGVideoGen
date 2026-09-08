package com.burmaldabot.dto.request.wan;

import java.util.List;

public record WanVideoInput(
        String prompt,
        List<String> reference_image_urls,
        String resolution,
        String aspect_ratio,
        Integer duration,
        Boolean audio,
        Boolean nsfw_checker
) {
}