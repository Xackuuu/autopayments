package com.bank.autopay.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@Slf4j
public class CronChecker implements CronService {
    @Override
    public boolean shouldExecuteNow(String cronExpression, LocalDateTime lastExecutedAt) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(
                cronExpression.split(" ").length == 5 ? CronType.UNIX : CronType.QUARTZ);

        CronParser parser = new CronParser(cronDefinition);

        Cron cron = parser.parse(cronExpression);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime now = ZonedDateTime.now();

        ZonedDateTime lastRun = (lastExecutedAt != null)
                ? lastExecutedAt.atZone(ZoneId.systemDefault())
                : now.minusSeconds(1);

        ZonedDateTime nextScheduled = executionTime.nextExecution(lastRun).orElse(null);

        return nextScheduled != null && !now.isBefore(nextScheduled) && !lastRun.isEqual(nextScheduled);
    }
}
