package com.bank.autopay.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter successfulPayments;
    private final Counter failedPayments;
    private final Timer paymentExecutionTime;

    public PaymentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.successfulPayments = Counter.builder("autopay.payments.successful")
                .description("Number of successful payments")
                .register(meterRegistry);
        this.failedPayments = Counter.builder("autopay.payments.failed")
                .description("Number of failed payments")
                .register(meterRegistry);
        this.paymentExecutionTime = Timer.builder("autopay.payments.execution.timer")
                .register(meterRegistry);
    }

    public void recordSuccessfulPayment() {
        successfulPayments.increment();
    }

    public void recordFailedPayment() {
        failedPayments.increment();
    }

    public Timer.Sample startPaymentTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPaymentTimer(Timer.Sample sample) {
        sample.stop(paymentExecutionTime);
    }
}
