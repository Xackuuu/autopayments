package com.bank.autopay.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "autopay_rule")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AutopayRuleEntity {

    /**
     * Уникальный id правила
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Id Клиента в банке
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    /**
     * Id получателя
     */
    @Column(name = "recipient_id",nullable = false)
    private Long recipientId;
    /**
     * Сумма списания
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    /**
     * Расписание (например "0 0 14 * * *" — каждый день в 14:00)
     */
    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;
    /**
     * Последнее успешное исполнение
     */
    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;
    /**
     * Активно ли правило
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled;
}
