package com.burmaldabot.flow.economy;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {

    Optional<UserBalance> findByTelegramUserIdAndBotId(Long telegramUserId, Long botId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM UserBalance b WHERE b.telegramUserId = :telegramUserId AND b.botId = :botId")
    Optional<UserBalance> findForUpdateByTelegramUserIdAndBotId(
            @Param("telegramUserId") Long telegramUserId,
            @Param("botId") Long botId
    );

    @Modifying
    @Query("UPDATE UserBalance b SET b.balanceTokens = b.balanceTokens - :tokens, b.updatedAt = :now " +
           "WHERE b.telegramUserId = :telegramUserId AND b.botId = :botId AND b.balanceTokens >= :tokens")
    int deductTokensAtomically(
            @Param("telegramUserId") Long telegramUserId,
            @Param("botId") Long botId,
            @Param("tokens") Long tokens,
            @Param("now") Instant now
    );
}
