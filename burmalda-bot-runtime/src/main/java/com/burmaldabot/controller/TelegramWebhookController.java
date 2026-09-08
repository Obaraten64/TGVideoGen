package com.burmaldabot.controller;

import com.burmaldabot.service.webhook.TelegramUpdateRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram/webhook")
public class TelegramWebhookController {

    private final TelegramUpdateRouterService updateRouterService;

    @PostMapping("/{botId}")
    public ResponseEntity<Void> receiveUpdate(
            @PathVariable Long botId,
            @RequestBody Update update) {

        updateRouterService.route(botId, update);

        return ResponseEntity.ok().build();
    }
}