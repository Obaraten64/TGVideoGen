package com.burmaldabot.flow.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserSessionRepository extends JpaRepository<BotUserSession, Long> {
    Optional<BotUserSession> findByBotIdAndTelegramUserId(Long botId, Long telegramUserId);
}
