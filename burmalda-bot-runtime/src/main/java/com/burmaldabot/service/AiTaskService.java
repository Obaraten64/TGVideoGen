package com.burmaldabot.service;

import com.burmaldabot.model.ai.AiTask;
import com.burmaldabot.model.ai.AiTaskStatus;
import com.burmaldabot.model.bot.BotContext;
import com.burmaldabot.repository.AiTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AiTaskService {
    private final AiTaskRepository aiTaskRepository;

    @Transactional
    public void createTask(String taskId, BotContext botContext) {
        AiTask aiTask = AiTask.builder()
                .aiTaskId(taskId)
                .botId(botContext.bot().getId())
                .chatId(botContext.chatId())
                .telegramUserId(botContext.telegramUserId())
                .status(AiTaskStatus.CREATED)
                .createdAt(Instant.now())
                .build();
        aiTaskRepository.save(aiTask);
    }
}
