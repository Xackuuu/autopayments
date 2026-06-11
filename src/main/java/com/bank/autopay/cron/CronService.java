package com.bank.autopay.cron;

import java.time.LocalDateTime;

public interface CronService {

    public boolean shouldExecuteNow(String cronExpression, LocalDateTime lastExecutedAt);
}
