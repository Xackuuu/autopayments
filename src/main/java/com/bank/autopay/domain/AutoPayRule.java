package com.bank.autopay.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AutoPayRule {

    /**
     * Уникальный id правила
     */
    private Long id;
    /**
     * Id Клиента в банке
     */
    private Long userId;
    /**
     * Id получателя
     */
    private Long recipientId;
    /**
     * Сумма списания
     */
    private BigDecimal amount;
    /**
     * Расписание (например "0 0 14 * * *" — каждый день в 14:00)
     */
    private String cronExpression;
    /**
     * Последнее успешное исполнение
     */
    private LocalDateTime lastExecutedAt;
    /**
     * Активно ли правило
     */
    private boolean enabled;
}
