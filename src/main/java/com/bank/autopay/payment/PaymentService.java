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

    {
        balances.put(1L, new BigDecimal("1000.00"));
        balances.put(2L, new BigDecimal("500.00"));
        balances.put(3L, new BigDecimal("1000.00"));
        balances.put(4L, new BigDecimal("500.00"));
    }

    public boolean withdraw(Long userId, Long recipientId, BigDecimal amount) {
        BigDecimal userBalance = balances.get(userId);

        if (userBalance == null || userBalance.compareTo(amount) < 0) {
            return false;
        }

        balances.put(recipientId, BigDecimal.ZERO);
        balances.put(userId, userBalance.subtract(amount));
        balances.put(recipientId, balances.get(recipientId).add(amount));
        return true;
    }
}
