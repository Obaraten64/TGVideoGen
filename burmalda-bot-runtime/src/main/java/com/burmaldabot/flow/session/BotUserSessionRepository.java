package com.burmaldabot.flow.session;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserSessionRepository extends JpaRepository<BotUserSession, Long> {

    Optional<BotUserSession> findByBotIdAndTelegramUserId(Long botId, Long telegramUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BotUserSession> findForUpdateByBotIdAndTelegramUserId(Long botId, Long telegramUserId);
}
