package com.burmaldabot.flow.economy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFreeTrialRepository extends JpaRepository<UserFreeTrial, Long> {
    boolean existsByTelegramUserIdAndBotId(Long telegramUserId, Long botId);
    boolean existsByTelegramUserId(Long telegramUserId);
}
