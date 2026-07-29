package com.bank.autopay;

import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import com.bank.autopay.exception.RuleNotFoundException;
import com.bank.autopay.executor.AutoPayService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AutoPayServiceImplTest {

    @Autowired
    private AutoPayService service;

    @Test
    void shouldCreateRule() {
        // given
        AutopayRuleRequest request = new AutopayRuleRequest();
        request.setUserId(1L);
        request.setRecipientId(2L);
        request.setAmount(new BigDecimal("100.00"));
        request.setCronExpression("0 0 12 * * ?");

        // when
        AutopayRuleResponse response = service.createRule(request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldGetRuleById() {
        // given
        AutopayRuleRequest request = createRequest();
        AutopayRuleResponse created = service.createRule(request);

        // when
        AutopayRuleResponse found = service.getRuleById(created.getId());

        // then
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getUserId()).isEqualTo(created.getUserId());
    }

    @Test
    void shouldThrowNotFoundWhenRuleDoesNotExist() {
        // when/then
        assertThatThrownBy(() -> service.getRuleById(999L))
                .isInstanceOf(RuleNotFoundException.class)
                .hasMessageContaining("Rule by id 999 not found");
    }

    @Test
    void shouldUpdateRule() {
        // given
        AutopayRuleRequest request = createRequest();
        AutopayRuleResponse created = service.createRule(request);

        // when
        request.setAmount(new BigDecimal("200.00"));
        AutopayRuleResponse updated = service.updateRuleById(created.getId(), request);

        // then
        assertThat(updated.getAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldDeleteRule() {
        // given
        AutopayRuleRequest request = createRequest();
        AutopayRuleResponse created = service.createRule(request);

        // when
        service.deleteRuleById(created.getId());

        // then
        assertThatThrownBy(() -> service.getRuleById(created.getId()))
                .isInstanceOf(RuleNotFoundException.class);
    }

    private AutopayRuleRequest createRequest() {
        AutopayRuleRequest request = new AutopayRuleRequest();
        request.setUserId(1L);
        request.setRecipientId(2L);
        request.setAmount(new BigDecimal("100.00"));
        request.setCronExpression("0 0 12 * * ?");
        return request;
    }
}