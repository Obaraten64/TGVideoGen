package com.burmaldabot.flow.economy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    Optional<UserBalance> findByTelegramUserIdAndBotId(Long telegramUserId, Long botId);
}
