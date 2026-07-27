package com.bank.autopay.scheduler.config;

import com.bank.autopay.scheduler.AutopayJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail autopayJobDetail() {
        return JobBuilder.newJob(AutopayJob.class)
                .withIdentity("autopayJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger autopayTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(autopayJobDetail())
                .withIdentity("autopayTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .build();
    }
}
