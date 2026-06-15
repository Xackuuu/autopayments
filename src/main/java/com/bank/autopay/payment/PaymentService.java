package com.bank.autopay.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Setter @Getter
public class PaymentService {

    private final Map<Long, BigDecimal> balances = new ConcurrentHashMap<>();

    public boolean withdraw(Long userId, Long recipientId, BigDecimal amount) {
        balances.putIfAbsent(userId, new BigDecimal("10000.00"));
        balances.putIfAbsent(recipientId, new BigDecimal("10000.00"));
        BigDecimal userBalance = balances.get(userId);

        if (userBalance == null || userBalance.compareTo(amount) < 0) {
            return false;
        }

        balances.put(userId, userBalance.subtract(amount));
        balances.put(recipientId, balances.get(recipientId).add(amount));
        return true;
    }
}
