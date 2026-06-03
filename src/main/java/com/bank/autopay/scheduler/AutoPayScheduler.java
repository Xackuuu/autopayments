package com.bank.autopay.scheduler;

import com.bank.autopay.executor.AutoPayService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AutoPayScheduler {

    private final AutoPayService autoPayService;

    @Scheduled(cron = "0/30 * * * * ?")
    private void execute() {
        autoPayService.getActiveRules().stream()
                .map(rule -> String.format("Scheduled check: rule id=%s, user=%s, amount=%s", rule.getId(), rule.getUserId(), rule.getAmount()))
                .forEach(log::info);
    }
}
