package com.burmaldabot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Deprecated(since = "This class is deprecated and will be removed in future versions, in favor of user's input")
@Configuration
@RequiredArgsConstructor
@Getter
public class AiRequestConfig {
    @Value("${ai.request.prompt}")
    private final String prompt;
    @Value("${ai.request.resolution}")
    private final String resolution;
    @Value("${ai.request.duration}")
    private final int duration;
}
