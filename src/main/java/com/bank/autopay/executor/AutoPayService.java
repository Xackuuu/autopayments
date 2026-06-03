package com.bank.autopay.executor;

import com.bank.autopay.domain.AutoPayRule;

import java.util.List;

public interface AutoPayService {
    List<AutoPayRule> getActiveRules();
}
