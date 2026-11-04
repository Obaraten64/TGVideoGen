package com.burmaldabot.repository;

import com.burmaldabot.model.ai.AiTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiTaskRepository extends JpaRepository<AiTask, Long> {

    Optional<AiTask> findByAiTaskId(String aiTaskId);

    List<AiTask> findTop5ByBotIdAndTelegramUserIdOrderByCreatedAtDesc(Long botId, Long telegramUserId);
}
