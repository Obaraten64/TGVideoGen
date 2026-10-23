package com.burmaldabot.flow.handler;

import com.burmaldabot.flow.CallbackQueryHandler;
import com.burmaldabot.flow.session.BotUserSession;
import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateCallbackHandler implements CallbackQueryHandler {

    private final VideoGenerationDispatcherService dispatcherService;

    private static final Map<String, String> TEMPLATE_PROMPTS = Map.of(
            "tpl:cyberpunk", "cyberpunk style, neon lights, futuristic city atmosphere, volumetric lighting, high quality 8k",
            "tpl:cinematic", "cinematic film aesthetic, movie still, dramatic lighting, 35mm cinematography, masterpiece",
            "tpl:anime", "anime style, vibrant colors, Makoto Shinkai aesthetic, smooth animation, beautiful art",
            "tpl:surreal", "surreal dreamscape, ethereal atmosphere, glowing ambient particles, fantasy masterpiece"
    );

    @Override
    public boolean supports(String callbackData) {
        return callbackData != null && callbackData.startsWith("tpl:");
    }

    @Override
    public void handleCallback(BotContext context, BotUserSession session, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String prompt = TEMPLATE_PROMPTS.getOrDefault(data, "cinematic atmosphere, high quality");
        dispatcherService.startGeneration(context, session, prompt);
    }
}
