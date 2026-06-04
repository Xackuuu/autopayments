package com.bank.autopay.controller;

import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import com.bank.autopay.executor.AutoPayService;
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
public class AutopayRuleController {

    private final AutoPayService service;

    @GetMapping
    public ResponseEntity<List<AutopayRuleResponse>> getAllRules() {
        return ResponseEntity.ok(service.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutopayRuleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRuleById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AutopayRuleResponse>> getByActiveRules() {
        return ResponseEntity.ok(service.getActiveRules());
    }

    @PostMapping
    public ResponseEntity<AutopayRuleResponse> createRule(@Valid @RequestBody AutopayRuleRequest request) {
        return ResponseEntity.ok(service.createRule(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutopayRuleResponse> updateRuleById(@PathVariable Long id, @Valid @RequestBody AutopayRuleRequest request) {
        return ResponseEntity.ok(service.updateRuleById(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.deleteRuleById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
