package com.bank.autopay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Schema(description = "Ответ с данными правила автоплатежа")
public class AutopayRuleResponse {

    @Schema(description = "Уникальный ID правила", example = "1")
    private Long id;

    @Schema(description = "ID клиента", example = "1")
    private Long userId;

    @Schema(description = "ID получателя", example = "2")
    private Long recipientId;

    @Schema(description = "Сумма списания", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Cron выражение", example = "0 0 12 * * ?")
    private String cronExpression;

    @Schema(description = "Дата последнего выполнения", example = "2026-07-29T14:00:00")
    private LocalDateTime lastExecutedAt;

    @Schema(description = "Активно ли правило", example = "true")
    private boolean enabled;
}