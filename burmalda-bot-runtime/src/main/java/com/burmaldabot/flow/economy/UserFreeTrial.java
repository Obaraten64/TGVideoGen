package com.burmaldabot.flow.economy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "user_free_trial",
        uniqueConstraints = @UniqueConstraint(columnNames = {"telegramUserId", "botId"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFreeTrial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private Long botId;

    @Column(nullable = false)
    private Instant usedAt;

    public UserFreeTrial(Long telegramUserId, Long botId) {
        this.telegramUserId = telegramUserId;
        this.botId = botId;
        this.usedAt = Instant.now();
    }
}
