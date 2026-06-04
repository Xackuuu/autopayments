package com.bank.autopay.executor;

import com.bank.autopay.domain.AutopayRuleEntity;

import java.util.List;

public interface AutoPayService {
    List<AutopayRuleEntity> getActiveRules();
}
