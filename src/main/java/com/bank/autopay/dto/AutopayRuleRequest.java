package com.bank.autopay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter @Getter
@NoArgsConstructor
@Schema(description = "Запрос на создание/обновление правила автоплатежа")
public class AutopayRuleRequest {

    @Schema(description = "ID клиента в банке", example = "1", required = true)
    @NotNull(message = "userId обязателен")
    private Long userId;

    @Schema(description = "ID получателя", example = "2", required = true)
    @NotNull(message = "recipientId обязателен")
    private Long recipientId;

    @Schema(description = "Сумма списания", example = "100.00", required = true, minimum = "0.01")
    @NotNull(message = "amount обязателен")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @Schema(description = "Cron выражение для расписания",
            example = "0 0 12 * * ?",
            pattern = "^(\\*|([0-9]|[1-5][0-9]))\\s+\\*\\s+\\*\\s+\\*\\s+\\*\\s+\\?$")
    @Pattern(regexp = "^(\\*|([0-9]|[1-5][0-9]))\\s+\\*\\s+\\*\\s+\\*\\s+\\*\\s+\\?$",
            message = "Некорректный cron expression")
    private String cronExpression;

    @Schema(description = "Активно ли правило", example = "true", defaultValue = "true")
    private boolean enabled;

    // ... конструкторы
}