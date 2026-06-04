package com.bank.autopay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AutopayRuleResponse {

    private Long id;
    private Long userId;
    private Long recipientId;
    private BigDecimal amount;
    private String cronExpression;
    private LocalDateTime lastExecutedAt;
    private boolean enabled;
}
