package com.burmaldabot.flow.economy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBalanceService {
    //TODO: Implement payment system to allow users to purchase additional tokens
    private static final long INITIAL_TOKENS = 150L;

    private final UserBalanceRepository balanceRepository;
    private final UserFreeTrialRepository freeTrialRepository;

    @Transactional
    public UserBalance getOrCreateBalance(Long telegramUserId, Long botId) {
        return balanceRepository.findByTelegramUserIdAndBotId(telegramUserId, botId)
                .orElseGet(() -> {
                    try {
                        return balanceRepository.save(new UserBalance(telegramUserId, botId, INITIAL_TOKENS));
                    } catch (Exception e) {
                        return balanceRepository.findByTelegramUserIdAndBotId(telegramUserId, botId)
                                .orElseThrow(() -> new IllegalStateException("Failed to get/create balance", e));
                    }
                });
    }

    @Transactional(readOnly = true)
    public long getBalance(Long telegramUserId, Long botId) {
        return balanceRepository.findByTelegramUserIdAndBotId(telegramUserId, botId)
                .map(UserBalance::getBalanceTokens)
                .orElse(INITIAL_TOKENS);
    }

    @Transactional
    public boolean deductTokens(Long telegramUserId, Long botId, long tokensToDeduct) {
        if (tokensToDeduct <= 0) {
            return true;
        }
        // Ensure the record exists
        getOrCreateBalance(telegramUserId, botId);

        // Atomic decrement query prevents race conditions / overdraft
        int updated = balanceRepository.deductTokensAtomically(
                telegramUserId,
                botId,
                tokensToDeduct,
                Instant.now()
        );
        return updated > 0;
    }

    @Transactional
    public void addTokens(Long telegramUserId, Long botId, long tokensToAdd) {
        UserBalance balance = getOrCreateBalance(telegramUserId, botId);
        balance.setBalanceTokens(balance.getBalanceTokens() + tokensToAdd);
        balance.setUpdatedAt(Instant.now());
        balanceRepository.save(balance);
    }

    @Transactional(readOnly = true)
    public boolean hasUsedFreeTrial(Long telegramUserId, Long botId) {
        return freeTrialRepository.existsByTelegramUserIdAndBotId(telegramUserId, botId);
    }

    @Transactional
    public boolean tryConsumeFreeTrial(Long telegramUserId, Long botId) {
        if (hasUsedFreeTrial(telegramUserId, botId)) {
            return false;
        }
        try {
            freeTrialRepository.saveAndFlush(new UserFreeTrial(telegramUserId, botId));
            return true;
        } catch (Exception e) {
            log.warn("Concurrent free trial usage detected for user {} on bot {}", telegramUserId, botId);
            return false;
        }
    }
}
