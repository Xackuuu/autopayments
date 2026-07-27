package com.bank.autopay.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_executions",
        schema = "autopayment"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecution {

    @Id
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    private String status;  // "SUCCESS" или "FAILED"

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "result")
    private String result;

    // Конструктор для создания новой записи
    public PaymentExecution(String idempotencyKey, Long userId, BigDecimal amount,
                            String status, String result) {
        this.idempotencyKey = idempotencyKey;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.result = result;
        this.executedAt = LocalDateTime.now();
    }
}