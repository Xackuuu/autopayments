package com.bank.autopay.repository;

import com.bank.autopay.domain.PaymentExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentExecutionRepository
        extends JpaRepository<PaymentExecution, String> {

    // JpaRepository уже имеет методы:
    // - save()
    // - findById()
    // - existsById()
    // - deleteById()

    // Если понадобится дополнительный поиск:
    // Optional<PaymentExecution> findByIdempotencyKeyAndUserId(String key, Long userId);
}