package com.burmaldabotmanager.repository;

import com.burmaldabotmanager.model.bot.Bot;
import com.burmaldabotmanager.model.bot.BotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BotRepository extends JpaRepository<Bot, Long> {

    Optional<Bot> findByTelegramBotId(Long telegramBotId);

    List<Bot> findAllByStatus(BotStatus status);

    Optional<Bot> findByUsername(String username);
}
