package com.bank.autopay.controller;

import com.bank.autopay.scheduler.AutopayJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AutopayJob scheduler;

    @PostMapping("/stop")
    public ResponseEntity<String> stopScheduler() {
        scheduler.shutdown();

        return ResponseEntity.ok("Scheduler stopping...");
    }

}
