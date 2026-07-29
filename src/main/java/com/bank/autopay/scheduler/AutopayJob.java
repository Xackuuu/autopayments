package com.bank.autopay.scheduler;

import com.bank.autopay.cron.CronChecker;
import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.logging.CorrelationIdFilter;
import com.bank.autopay.monitoring.PaymentMetrics;
import com.bank.autopay.payment.PaymentService;
import com.bank.autopay.repository.AutoPayRuleRepository;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        // Генерируем correlationId для каждого запуска шедулера
        String correlationId = "scheduler-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC, correlationId);

        try {
            if (!running) {
                log.info("Scheduler is stopping, skipping execution");
                return;
            }

            log.info("Scheduler started");
            List<AutopayRuleEntity> activeRules = repository.findByEnabledTrue();
            log.info("Found {} active rules", activeRules.size());

            activeRules.forEach(entity -> {
                if (cronChecker.shouldExecuteNow(entity.getCronExpression(), entity.getLastExecutedAt())) {
                    executePayment(entity);
                }
            });

            log.info("Scheduler completed");

        } catch (Exception e) {
            log.error("Scheduler execution failed", e);
            throw e;
        } finally {
            // Удаляем из MDC после завершения
            MDC.clear();
        }
    }

    /**
     * Выполняет платеж с использованием идемпотентности
     * Генерирует уникальный ключ для каждого платежа
     */
    @Transactional
    private void executePayment(AutopayRuleEntity rule) {
        // Добавляем ruleId в MDC для детализации
        MDC.put("ruleId", rule.getId().toString());

        Timer.Sample timer = paymentMetrics.startPaymentTimer();

        try {
            // ... существующий код ...
            log.info("Payment executed: userId={}, amount={}",
                    rule.getUserId(), rule.getAmount());
        } finally {
            paymentMetrics.stopPaymentTimer(timer);
            // Удаляем ruleId из MDC
            MDC.remove("ruleId");
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down AutopayJob...");
        running = false;
        log.info("✅ AutopayJob shutdown complete");
    }
}