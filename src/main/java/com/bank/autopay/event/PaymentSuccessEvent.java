package com.bank.autopay.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter @Setter
public class PaymentSuccessEvent {

    private final Long ruleId;
    private final Long userId;
    private final BigDecimal amount;
}
