package com.burmaldabot.model.ai;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ai_task")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String aiTaskId;

    @Column(nullable = false)
    private Long botId;

    @Column(nullable = false)
    private Long chatId;

    private Long telegramUserId;

    @Enumerated(EnumType.STRING)
    private AiTaskStatus status;

    private Instant createdAt;
    private Instant completedAt;
}
