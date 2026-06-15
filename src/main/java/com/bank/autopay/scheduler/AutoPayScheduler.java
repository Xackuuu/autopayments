package com.bank.autopay.scheduler;

import com.bank.autopay.cron.CronChecker;
import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.payment.PaymentService;
import com.bank.autopay.repository.AutoPayRuleRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class AutoPayScheduler {

    private final PaymentService paymentService;
    private final AutoPayRuleRepository repository;
    private final CronChecker cronChecker;

    @Scheduled(cron = "0/10 * * * * ?")
    protected void execute() {
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

    @Transactional
    private void executePayment(AutopayRuleEntity rule) {

        try {
            boolean result = paymentService.withdraw(rule.getUserId(), rule.getRecipientId(), rule.getAmount());
            if (result) {
                rule.setLastExecutedAt(LocalDateTime.now());
                repository.save(rule);
                log.info("Payment executed: ruleId={}, userId={}, amount={}, balance={}", rule.getId(), rule.getUserId(), rule.getAmount(), paymentService.getBalances().get(rule.getUserId()));
            } else {
                log.error("Insufficient funds: userId={}, balance={}, required={}", rule.getUserId(), paymentService.getBalances().get(rule.getUserId()), rule.getAmount());
            }
        } catch (OptimisticLockException e) {
            log.warn("Rule {} was updated concurrently, skipping this cycle", rule.getId());
        }



    }
}
