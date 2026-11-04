package com.burmaldabot.flow.session;

import com.burmaldabot.model.bot.BotContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSessionService {

    private final BotUserSessionRepository sessionRepository;

    @Transactional
    public BotUserSession getOrCreate(BotContext context) {
        return sessionRepository.findForUpdateByBotIdAndTelegramUserId(context.bot().getId(), context.telegramUserId())
                .map(session -> {
                    session.setChatId(context.chatId());
                    session.setUpdatedAt(Instant.now());
                    return sessionRepository.save(session);
                })
                .orElseGet(() -> {
                    try {
                        BotUserSession newSession = new BotUserSession(
                                context.bot().getId(),
                                context.telegramUserId(),
                                context.chatId()
                        );
                        return sessionRepository.save(newSession);
                    } catch (Exception e) {
                        // In case of concurrent creation race, fallback to locked read
                        return sessionRepository.findForUpdateByBotIdAndTelegramUserId(context.bot().getId(), context.telegramUserId())
                                .orElseThrow(() -> new IllegalStateException("Failed to get or create session", e));
                    }
                });
    }

    @Transactional
    public BotUserSession save(BotUserSession session) {
        session.setUpdatedAt(Instant.now());
        return sessionRepository.save(session);
    }
}
