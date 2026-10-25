package com.burmaldabot.flow.economy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "user_balance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"telegramUserId", "botId"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private Long botId;

    @Column(nullable = false)
    private Long balanceTokens;

    @Version
    private Long version;

    @Column(nullable = false)
    private Instant updatedAt;

    public UserBalance(Long telegramUserId, Long botId, Long initialTokens) {
        this.telegramUserId = telegramUserId;
        this.botId = botId;
        this.balanceTokens = initialTokens;
        this.updatedAt = Instant.now();
    }
}
