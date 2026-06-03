package com.bank.autopay.executor;

import com.bank.autopay.domain.AutoPayRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AutoPayServiceImpl implements AutoPayService {

    private final Map<Long, AutoPayRule> rules = new HashMap<>();

    public AutoPayServiceImpl() {
        rules.put(1L, new AutoPayRule(1L, 1L, 2L, new BigDecimal("100"), "0 0 14 * * *", LocalDateTime.now(), true));
        rules.put(2L, new AutoPayRule(2L, 2L, 3L, new BigDecimal("100"), "0 0 14 * * *", LocalDateTime.now(), false));
        rules.put(3L, new AutoPayRule(3L, 3L, 4L, new BigDecimal("100"), "0 0 14 * * *", LocalDateTime.now(), true));
        rules.put(4L, new AutoPayRule(4L, 4L, 1L, new BigDecimal("100"), "0 0 14 * * *", LocalDateTime.now(), false));
    }

    @Override
    public List<AutoPayRule> getActiveRules() {
        log.debug("Scanning active rules...");
        return rules.values().stream()
                .filter(AutoPayRule::isEnabled)
                .collect(Collectors.toList());
    }
}
