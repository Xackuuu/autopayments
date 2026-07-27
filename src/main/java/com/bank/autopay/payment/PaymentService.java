package com.bank.autopay.payment;

import com.bank.autopay.domain.PaymentExecution;
import com.bank.autopay.repository.PaymentExecutionRepository;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Setter @Getter
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentExecutionRepository executionRepository;

    // Хранилище балансов (временное, для демонстрации)
    private final Map<Long, BigDecimal> balances = new ConcurrentHashMap<>();
    {
        balances.put(1L, new BigDecimal("10000.00"));
        balances.put(2L, new BigDecimal("10000.00"));
        balances.put(3L, new BigDecimal("10000.00"));
        balances.put(4L, new BigDecimal("10000.00"));
        balances.put(5L, new BigDecimal("10000.00"));
    }

    /**
     * Идемпотентный платеж с автоматическими повторами при ошибках БД
     *
     * @Retryable — повторяем метод при ошибках
     * value = DataAccessException.class — только при ошибках БД
     * maxAttempts = 3 — максимум 3 попытки (1 основная + 2 повтора)
     * backoff — настройка задержки между попытками
     */
    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,      // 1 секунда
                    multiplier = 2     // увеличиваем задержку: 1с → 2с → 4с
            )
    )
    @Transactional
    public boolean processPayment(String idempotencyKey, Long userId, Long recipientId, BigDecimal amount) {
        log.info("Processing payment with key: {}, userId: {}, amount: {}",
                idempotencyKey, userId, amount);

        // ШАГ 1: Проверяем, не выполнялся ли уже этот платеж
        Optional<PaymentExecution> existing = executionRepository.findById(idempotencyKey);

        if (existing.isPresent()) {
            // Платеж уже был выполнен (или была попытка)
            PaymentExecution execution = existing.get();
            log.info("Idempotent request detected: key={}, status={}",
                    idempotencyKey, execution.getStatus());

            // Если платеж был успешным — возвращаем true
            if ("SUCCESS".equals(execution.getStatus())) {
                log.info("Returning cached SUCCESS result for key: {}", idempotencyKey);
                return true;
            }

            // Если платеж был неудачным — возвращаем false, но не повторяем
            log.info("Returning cached FAILED result for key: {}", idempotencyKey);
            return false;
        }

        // ШАГ 2: Выполняем сам платеж
        try {
            // 2.1: Проверяем баланс
            BigDecimal userBalance = balances.get(userId);
            if (userBalance == null || userBalance.compareTo(amount) < 0) {
                // Недостаточно средств — сохраняем как FAILED и возвращаем false
                saveExecution(idempotencyKey, userId, amount, "FAILED",
                        "Insufficient funds: " + userBalance);
                return false;
            }

            // 2.2: Списываем деньги
            balances.putIfAbsent(recipientId, BigDecimal.ZERO);
            balances.put(userId, userBalance.subtract(amount));
            balances.put(recipientId, balances.get(recipientId).add(amount));

            // 2.3: Сохраняем успешный результат
            saveExecution(idempotencyKey, userId, amount, "SUCCESS",
                    "Transaction completed successfully");

            log.info("Payment completed: userId={}, amount={}, balance={}",
                    userId, amount, balances.get(userId));

            return true;

        } catch (Exception e) {
            // Любая ошибка — сохраняем как FAILED
            saveExecution(idempotencyKey, userId, amount, "FAILED", e.getMessage());
            log.error("Payment failed: {}", e.getMessage());
            throw e;  // Перебрасываем для Retry
        }
    }

    /**
     * Сохраняем результат платежа в БД
     */
    @Transactional
    protected void saveExecution(String key, Long userId, BigDecimal amount,
                                 String status, String result) {
        PaymentExecution execution = new PaymentExecution(key, userId, amount, status, result);
        executionRepository.save(execution);
        log.debug("Saved execution: key={}, status={}", key, status);
    }

    /**
     * Метод восстановления — вызывается, когда все попытки Retry исчерпаны
     *
     * @Recover — обрабатывает исключение после исчерпания всех попыток
     * Параметры должны совпадать с @Retryable методом
     */
    @Recover
    public boolean recover(DataAccessException e, String idempotencyKey,
                           Long userId, Long recipientId, BigDecimal amount) {
        log.error("All retry attempts failed for payment: key={}, userId={}, error: {}",
                idempotencyKey, userId, e.getMessage());

        // Сохраняем как FAILED, если ещё не сохранили
        try {
            if (!executionRepository.existsById(idempotencyKey)) {
                saveExecution(idempotencyKey, userId, amount, "FAILED",
                        "All retry attempts failed: " + e.getMessage());
            }
        } catch (Exception ex) {
            log.error("Failed to save recovery result: {}", ex.getMessage());
        }

        // Возвращаем false (платеж не выполнен)
        return false;
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down PaymentService...");
        log.info("📊 Final balances: {}", balances);
        log.info("✅ PaymentService shutdown complete");
    }
}