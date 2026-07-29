package com.bank.autopay;

import com.bank.autopay.scheduler.AutopayJob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatCode;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AutopayJobTest {

    @Autowired
    private AutopayJob autopayJob;

    @Test
    void shouldExecuteWithoutErrors() {
        // when/then
        assertThatCode(() -> autopayJob.execute(null))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldSkipExecutionWhenStopped() {
        // given
        autopayJob.shutdown();

        // when/then
        assertThatCode(() -> autopayJob.execute(null))
                .doesNotThrowAnyException();
    }
}