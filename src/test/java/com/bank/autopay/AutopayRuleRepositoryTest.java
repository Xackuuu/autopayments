package com.bank.autopay;

import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.repository.AutoPayRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AutopayRuleRepositoryTest {

    @Autowired
    private AutoPayRuleRepository repository;

    @Test
    @Transactional
    void shouldSaveAndFindRule() {
        AutopayRuleEntity entity = createRule();

        AutopayRuleEntity saved = repository.save(entity);

        log.info("\n✅ Сущность сохранена с ID: {}", saved.getId());
        log.info("\n✅ CreatedAt: {}", saved.getCreatedAt());
        log.info("\n✅ DeletedAt: {}", saved.getDeletedAt());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();


        Optional<AutopayRuleEntity> found = repository.findById(saved.getId());

        if (found.isPresent()) {
            log.info("\n✅ Сущность создана и найдена");
        } else {
            log.error("\n❌ Сущность не создана и не найдена");
        }

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(5L);
    }

    @Test
    @Transactional
    void softDeleteShouldSetDeletedAt() {
        AutopayRuleEntity rule = repository.save(createRule());

        log.info("\n✅ Сущность сохранена с ID: {}", rule.getId());
        log.info("\n✅ CreatedAt: {}", rule.getCreatedAt());
        log.info("\n✅ DeletedAt: {}", rule.getDeletedAt());

        Optional<AutopayRuleEntity> beforeDelete = repository.findById(rule.getId());
        if (beforeDelete.isPresent()) {
            log.info("\n✅ Сущность найдена {}", beforeDelete.get());
        } else {
            log.error("\n❌ Сущность не найдена!!!");
        }


        log.info("\n Удаление сущности с ID: {}", rule.getId());
        repository.softDeleteById(rule.getId());
        Optional<AutopayRuleEntity> afterDelete = repository.findById(rule.getId());

        if (afterDelete.isEmpty()) {
            log.info("\n✅ Сущность с ID: {} скрыта", rule.getId());
        } else {
            log.error("\n❌ СУЩНОСТЬ ВСЕ ЕЩЕ НАЙДЕНА!");
            log.error("\n❌ Мягкое удаление НЕ сработало!");
        }

        assertThat(afterDelete).isEmpty();
    }

    private AutopayRuleEntity createRule() {
        AutopayRuleEntity entity = new AutopayRuleEntity();
        entity.setUserId(5L);
        entity.setRecipientId(1L);
        entity.setAmount(new BigDecimal("400.00"));
        entity.setCronExpression("50 * * * * ?");
        entity.setEnabled(true);
        return entity;
    }
}
