package com.burmaldabot.controller;

import com.burmaldabot.dto.response.callback.KieCallback;
import com.burmaldabot.service.KieCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/kie")
@RequiredArgsConstructor
public class KieCallbackController {
    private final KieCallbackService kieCallbackService;

    @PostMapping("/callback/{chatId}")
    public ResponseEntity<Void> callback(@PathVariable String chatId, @RequestBody KieCallback payload) {
        log.info("Handling video response from AI and user {}", chatId);

        kieCallbackService.handle(payload, chatId);

        return ResponseEntity.ok().build();
    }
}
