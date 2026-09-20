package com.burmaldabot.controller;

import com.burmaldabot.service.webhook.TelegramUpdateRouterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram/webhook")
public class TelegramWebhookController {

    private final TelegramUpdateRouterService updateRouterService;
    private final com.fasterxml.jackson.databind.ObjectMapper jackson2Mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @PostMapping("/{botId}")
    public ResponseEntity<Void> receiveUpdate(
            @PathVariable Long botId,
            @RequestBody String payload) throws JsonProcessingException {

        Update update = jackson2Mapper.readValue(payload, Update.class);
        updateRouterService.route(botId, update);

        return ResponseEntity.ok().build();
    }
}