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

    private static final long INITIAL_TOKENS = 150L;

    private final UserBalanceRepository balanceRepository;
    private final UserFreeTrialRepository freeTrialRepository;

    @Transactional
    public UserBalance getOrCreateBalance(Long telegramUserId, Long botId) {
        return balanceRepository.findByTelegramUserIdAndBotId(telegramUserId, botId)
                .orElseGet(() -> balanceRepository.save(new UserBalance(telegramUserId, botId, INITIAL_TOKENS)));
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
        UserBalance balance = getOrCreateBalance(telegramUserId, botId);
        if (balance.getBalanceTokens() < tokensToDeduct) {
            return false;
        }
        balance.setBalanceTokens(balance.getBalanceTokens() - tokensToDeduct);
        balance.setUpdatedAt(Instant.now());
        balanceRepository.save(balance);
        return true;
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
    public void recordFreeTrialUsage(Long telegramUserId, Long botId) {
        if (!hasUsedFreeTrial(telegramUserId, botId)) {
            freeTrialRepository.save(new UserFreeTrial(telegramUserId, botId));
        }
    }
}
