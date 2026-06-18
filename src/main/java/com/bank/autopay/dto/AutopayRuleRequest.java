package com.bank.autopay.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter @Getter
@NoArgsConstructor
public class AutopayRuleRequest {

    /**
     * Id Клиента в банке
     */
    @NotNull(message = "userId обязателен")
    private Long userId;
    /**
     * Id получателя
     */
    @NotNull(message = "recipientId обязателен")
    private Long recipientId;
    /**
     * Сумма списания
     */
    @NotNull(message = "amount обязателен")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @Pattern(regexp = "^(\\*|([0-9]|[1-5][0-9]))\\s+\\*\\s+\\*\\s+\\*\\s+\\*\\s+\\?$", message = "Некорректный cron expression")
    private String cronExpression;
    /**
     * Активно ли правило
     */
    private boolean enabled;

    public AutopayRuleRequest(Long userId, Long recipientId, BigDecimal amount, String cronExpression) {
        this.userId = userId;
        this.recipientId = recipientId;
        this.amount = amount;
        this.cronExpression = cronExpression;
        this.enabled = true;
    }
}
