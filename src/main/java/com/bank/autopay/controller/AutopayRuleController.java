package com.bank.autopay.controller;

import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import com.bank.autopay.executor.AutoPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Rules", description = "Управление правилами автоплатежей")
public class AutopayRuleController {

    private final AutoPayService service;

    @Operation(
            summary = "Получить все правила",
            description = "Возвращает список всех правил автоплатежей (активные и неактивные). " +
                    "Мягко удалённые правила не отображаются."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный запрос",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AutopayRuleResponse.class)
            )
    )
    @GetMapping
    public ResponseEntity<List<AutopayRuleResponse>> getAllRules() {
        log.info("GET /api/rules - getting all rules");
        return ResponseEntity.ok(service.getAllRules());
    }

    @Operation(
            summary = "Получить правило по ID",
            description = "Возвращает правило автоплатежа по его идентификатору"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Правило найдено"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Правило не найдено или было удалено",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Rule by id 999 not found"
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AutopayRuleResponse> getById(
            @Parameter(description = "ID правила", example = "1", required = true)
            @PathVariable Long id) {
        log.info("GET /api/rules/{} - getting rule by id", id);
        return ResponseEntity.ok(service.getRuleById(id));
    }

    @Operation(
            summary = "Получить активные правила",
            description = "Возвращает список только активных (enabled=true) правил автоплатежей"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный запрос"
    )
    @GetMapping("/active")
    public ResponseEntity<List<AutopayRuleResponse>> getByActiveRules() {
        log.info("GET /api/rules/active - getting active rules");
        return ResponseEntity.ok(service.getActiveRules());
    }

    @Operation(
            summary = "Создать новое правило",
            description = "Создаёт новое правило автоплатежа. " +
                    "Правило может быть создано как активным (enabled=true), так и неактивным (enabled=false). " +
                    "По умолчанию правило создаётся активным."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Правило создано"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные (сумма <= 0, неверный cron)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"status\":400,\"message\":\"Validation failed\",\"timestamp\":123456789,\"errors\":{\"amount\":\"Сумма должна быть больше 0\"}}"
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<AutopayRuleResponse> createRule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания правила",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "{\"userId\":1,\"recipientId\":2,\"amount\":100.00,\"cronExpression\":\"0 0 12 * * ?\",\"enabled\":true}"
                            )
                    )
            )
            @Valid @RequestBody AutopayRuleRequest request) {
        log.info("POST /api/rules - creating rule for userId: {}, amount: {}",
                request.getUserId(), request.getAmount());
        return ResponseEntity.ok(service.createRule(request));
    }

    @Operation(
            summary = "Обновить правило",
            description = "Обновляет существующее правило автоплатежа. " +
                    "Все поля должны быть переданы (полное обновление)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Правило обновлено"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Правило не найдено"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<AutopayRuleResponse> updateRuleById(
            @Parameter(description = "ID правила", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AutopayRuleRequest request) {
        log.info("PUT /api/rules/{} - updating rule", id);
        return ResponseEntity.ok(service.updateRuleById(id, request));
    }

    @Operation(
            summary = "Удалить правило",
            description = "Мягкое удаление правила (устанавливается deletedAt). " +
                    "Правило можно восстановить через отдельный эндпоинт."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Правило удалено"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Правило не найдено"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Правило уже было удалено"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID правила", example = "1", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/rules/{} - soft deleting rule", id);
        service.deleteRuleById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Восстановить удалённое правило",
            description = "Восстанавливает мягко удалённое правило (устанавливает deletedAt = NULL)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Правило восстановлено"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Правило не найдено"
            )
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<AutopayRuleResponse> restoreRuleById(
            @Parameter(description = "ID правила", example = "1", required = true)
            @PathVariable Long id) {
        log.info("PATCH /api/rules/{}/restore - restoring soft-deleted rule", id);
        return ResponseEntity.ok(service.restoreRuleById(id));
    }
}