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
        activeRules.forEach(
                entity ->
                {
                    if (cronChecker.shouldExecuteNow(entity.getCronExpression(), entity.getLastExecutedAt())) {
                        executePayment(entity);
                    }
                }
        );
    }

    @PreDestroy
    public void shutdown() {
        log.info("🛑 Shutting down AutoPayScheduler...");
        running = false;
        // Ждём завершения текущих операций (уже делает spring.lifecycle.timeout)
        log.info("✅ AutoPayScheduler shutdown complete");
    }

    @Transactional
    private void executePayment(AutopayRuleEntity rule) {
        Timer.Sample timer = paymentMetrics.startPaymentTimer();
        try {
            boolean result = paymentService.withdraw(rule.getUserId(), rule.getRecipientId(), rule.getAmount());
            if (result) {
                rule.setLastExecutedAt(LocalDateTime.now());
                repository.save(rule);
                paymentMetrics.recordSuccessfulPayment();
                applicationEventPublisher.publishEvent(
                        new PaymentSuccessEvent(
                                rule.getId(),
                                rule.getUserId(),
                                rule.getAmount()
                        ));

                log.info("Payment executed: ruleId={}, userId={}, amount={}, balance={}", rule.getId(), rule.getUserId(), rule.getAmount(), paymentService.getBalances().get(rule.getUserId()));
            } else {
                paymentMetrics.recordFailedPayment();

                applicationEventPublisher.publishEvent(
                        new PaymentFailedEvent(
                                rule.getId(),
                                rule.getUserId(),
                                rule.getAmount(),
                                "Insufficient funds"
                        )
                );

                log.error("Insufficient funds: userId={}, balance={}, required={}", rule.getUserId(), paymentService.getBalances().get(rule.getUserId()), rule.getAmount());
            }
        } catch (OptimisticLockException e) {
            paymentMetrics.recordFailedPayment();

            applicationEventPublisher.publishEvent(new PaymentFailedEvent(
                    rule.getId(),
                    rule.getUserId(),
                    rule.getAmount(),
                    "Optimistic lock exception: " + e.getMessage()
            ));

            log.warn("Rule {} was updated concurrently, skipping this cycle", rule.getId());
        } finally {
            paymentMetrics.stopPaymentTimer(timer);
        }
    }
}
