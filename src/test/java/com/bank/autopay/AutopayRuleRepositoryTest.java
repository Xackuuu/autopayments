package com.bank.autopay;

import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.repository.AutoPayRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AutopayRuleRepositoryTest {

    @Autowired
    private AutoPayRuleRepository repository;

    @Test
    void shouldSaveAndFindRule() {
        // given
        AutopayRuleEntity rule = new AutopayRuleEntity();
        rule.setUserId(1L);
        rule.setRecipientId(2L);
        rule.setAmount(new BigDecimal("100.00"));
        rule.setCronExpression("0 0 12 * * ?");
        rule.setEnabled(true);

        // when
        AutopayRuleEntity saved = repository.save(rule);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);

        // when
        Optional<AutopayRuleEntity> found = repository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldFindOnlyActiveRules() {
        // given
        AutopayRuleEntity activeRule = createRule(true);
        AutopayRuleEntity inactiveRule = createRule(false);
        repository.save(activeRule);
        repository.save(inactiveRule);

        // when
        List<AutopayRuleEntity> activeRules = repository.findByEnabledTrue();

        // then
        assertThat(activeRules).contains(activeRule);
        assertThat(activeRules).doesNotContain(inactiveRule);
    }

    @Test
    void shouldSoftDeleteRule() {
        // given
        AutopayRuleEntity rule = repository.save(createRule(true));

        // when
        repository.softDeleteById(rule.getId());

        // then
        Optional<AutopayRuleEntity> found = repository.findById(rule.getId());
        assertThat(found).isEmpty();

        // Восстанавливаем для проверки deletedAt
        AutopayRuleEntity deleted = repository.findByIdWithDeleted(rule.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    private AutopayRuleEntity createRule(boolean enabled) {
        AutopayRuleEntity rule = new AutopayRuleEntity();
        rule.setUserId(1L);
        rule.setRecipientId(2L);
        rule.setAmount(new BigDecimal("100.00"));
        rule.setCronExpression("0 0 12 * * ?");
        rule.setEnabled(enabled);
        return rule;
    }
}