package com.burmaldabotmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BotUrlService {
    @Value("${telegram.manager.username}")
    private final String telegramManagerUsername;
    @Value("${telegram.managed.adjective}")
    private final List<String> adjectives;
    @Value("${telegram.managed.noun}")
    private final List<String> nouns;
    private final SecureRandom random = new SecureRandom();

    public String prepareBotCreationUrl() {
        String suggestedBotUsername = getSuggestedBotId() + "_bot";
        String encodedName = URLEncoder.encode(getSuggestedBotUsername(), StandardCharsets.UTF_8);
        return  "https://t.me/newbot/%s/%s?name=%s".formatted(telegramManagerUsername, suggestedBotUsername, encodedName);
    }

    private String getSuggestedBotUsername() {
        return adjectives.get(random.nextInt(adjectives.size())) + " " +
                nouns.get(random.nextInt(nouns.size()));
    }

    private String getSuggestedBotId() {
        return random.ints(8, 0, 26)
                .mapToObj(i -> String.valueOf((char) ('a' + i)))
                .collect(Collectors.joining());
    }
}
