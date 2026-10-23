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
        return sessionRepository.findByBotIdAndTelegramUserId(context.bot().getId(), context.telegramUserId())
                .map(session -> {
                    session.setChatId(context.chatId());
                    session.setUpdatedAt(Instant.now());
                    return sessionRepository.save(session);
                })
                .orElseGet(() -> {
                    BotUserSession newSession = new BotUserSession(
                            context.bot().getId(),
                            context.telegramUserId(),
                            context.chatId()
                    );
                    return sessionRepository.save(newSession);
                });
    }

    @Transactional
    public BotUserSession save(BotUserSession session) {
        session.setUpdatedAt(Instant.now());
        return sessionRepository.save(session);
    }
}
