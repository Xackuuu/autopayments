package com.bank.autopay.scheduler;

import com.bank.autopay.cron.CronChecker;
import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.event.PaymentFailedEvent;
import com.bank.autopay.event.PaymentSuccessEvent;
import com.bank.autopay.monitoring.PaymentMetrics;
import com.bank.autopay.payment.PaymentService;
import com.bank.autopay.repository.AutoPayRuleRepository;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutopayJob implements Job {

    private final AutoPayRuleRepository repository;
    private final PaymentMetrics paymentMetrics;
    private final PaymentService paymentService;
    private final CronChecker cronChecker;
    private final ApplicationEventPublisher applicationEventPublisher;
    private volatile boolean running = true;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!running) {
            log.info("Scheduler is stopping, skipping execution");
            return;
        }

        List<AutopayRuleEntity> activeRules = repository.findByEnabledTrue();
        activeRules.forEach(entity -> {
            if (cronChecker.shouldExecuteNow(entity.getCronExpression(), entity.getLastExecutedAt())) {
                executePayment(entity);
            }
        });
    }

    /**
     * Выполняет платеж с использованием идемпотентности
     * Генерирует уникальный ключ для каждого платежа
     */
    @Transactional
    private void executePayment(AutopayRuleEntity rule) {
        // Генерируем уникальный ключ для платежа
        // Формат: "rule-{ruleId}-{timestamp}-{UUID}"
        String idempotencyKey = String.format("rule-%d-%d-%s",
                rule.getId(),
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8)
        );

        Timer.Sample timer = paymentMetrics.startPaymentTimer();

        try {
            // Вызываем идемпотентный платеж с Retry
            boolean result = paymentService.processPayment(
                    idempotencyKey,
                    rule.getUserId(),
                    rule.getRecipientId(),
                    rule.getAmount()
            );

            if (result) {
                // Успешный платеж
                rule.setLastExecutedAt(LocalDateTime.now());
                repository.save(rule);
                paymentMetrics.recordSuccessfulPayment();

                applicationEventPublisher.publishEvent(
                        new PaymentSuccessEvent(rule.getId(), rule.getUserId(), rule.getAmount())
                );

                log.info("Payment executed: ruleId={}, userId={}, amount={}, balance={}",
                        rule.getId(), rule.getUserId(), rule.getAmount(),
                        paymentService.getBalances().get(rule.getUserId()));
            } else {
                // Неудачный платеж (недостаточно средств или ошибка)
                paymentMetrics.recordFailedPayment();

                applicationEventPublisher.publishEvent(
                        new PaymentFailedEvent(rule.getId(), rule.getUserId(), rule.getAmount(),
                                "Payment failed (insufficient funds or error)")
                );

                log.error("Payment failed: ruleId={}, userId={}, amount={}",
                        rule.getId(), rule.getUserId(), rule.getAmount());
            }

        } catch (OptimisticLockException e) {
            // Конкурентное обновление правила
            paymentMetrics.recordFailedPayment();

            applicationEventPublisher.publishEvent(new PaymentFailedEvent(
                    rule.getId(), rule.getUserId(), rule.getAmount(),
                    "Optimistic lock exception: " + e.getMessage()
            ));

            log.warn("Rule {} was updated concurrently, skipping this cycle", rule.getId());

        } catch (Exception e) {
            // Неожиданная ошибка
            paymentMetrics.recordFailedPayment();

            applicationEventPublisher.publishEvent(new PaymentFailedEvent(
                    rule.getId(), rule.getUserId(), rule.getAmount(),
                    "Unexpected error: " + e.getMessage()
            ));

            log.error("Unexpected error executing payment: ruleId={}, error={}",
                    rule.getId(), e.getMessage(), e);

        } finally {
            paymentMetrics.stopPaymentTimer(timer);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down AutopayJob...");
        running = false;
        log.info("✅ AutopayJob shutdown complete");
    }
}