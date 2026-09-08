package com.burmaldabotmanager.model.bot;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bot")
@Data
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_bot_id", nullable = false, unique = true)
    private Long telegramBotId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotStatus status;
}