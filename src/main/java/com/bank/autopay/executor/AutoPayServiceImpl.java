package com.bank.autopay.executor;

import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.repository.AutoPayRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoPayServiceImpl implements AutoPayService {

    private final AutoPayRuleRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AutopayRuleEntity> getActiveRules() {
        log.debug("Scanning active rules...");
        return repository.findByEnabledTrue();
    }
}
