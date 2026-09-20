package com.burmaldabot.repository;

import com.burmaldabot.model.ai.AiTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiTaskRepository extends JpaRepository<AiTask, Long> {

    Optional<AiTask> findByAiTaskId(String aiTaskId);

}
