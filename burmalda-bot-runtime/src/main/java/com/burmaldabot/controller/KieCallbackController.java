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

    @PostMapping("/callback/")
    public ResponseEntity<Void> callback( @RequestBody KieCallback payload) {
        log.info("Handling video response from AI");

        kieCallbackService.handle(payload);

        return ResponseEntity.ok().build();
    }
}
