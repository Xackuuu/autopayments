package com.bank.autopay.executor;

import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;

import java.util.List;

public interface AutoPayService {

    /**
     * Получение только активных правил
     * @return
     */
    List<AutopayRuleResponse> getActiveRules();

    /**
     * Получение всех правил
     * @return
     */
    List<AutopayRuleResponse> getAllRules();

    /**
     * Получение определенного правила по id
     * @param id
     * @return
     */
    AutopayRuleResponse getRuleById(Long id);

    /**
     * Создание правила
     * @return
     */
    AutopayRuleResponse createRule(AutopayRuleRequest request);

    /**
     * Обновления правила
     * @param id
     * @param request
     * @return
     */
    AutopayRuleResponse updateRuleById(Long id, AutopayRuleRequest request);

    /**
     * Удаление правила по id
     * @param id
     */
    void deleteRuleById(Long id);
}
