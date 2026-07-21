package com.bank.autopay.event.listener;

import com.bank.autopay.event.PaymentFailedEvent;
import com.bank.autopay.event.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentEventListener {

    @Async
    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("📧 Sending success notification for rule {}: user {}, amount {}",
                event.getRuleId(), event.getUserId(), event.getAmount());
    }

    @Async
    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("📧 Sending failure notification for rule {}: {}",
                event.getRuleId(), event.getReason());
    }
}
