package com.bank.autopay.executor;

import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import com.bank.autopay.dto.mapper.AutopayRuleMapper;
import com.bank.autopay.exception.RuleAlreadyDeletedException;
import com.bank.autopay.exception.RuleNotFoundException;
import com.bank.autopay.repository.AutoPayRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoPayServiceImpl implements AutoPayService {

    private final AutoPayRuleRepository repository;
    private final AutopayRuleMapper mapper;

    @Override
    @Cacheable(value = "activeRules", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<AutopayRuleResponse> getActiveRules() {
        return mapper.toDtoList(repository.findByEnabledTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutopayRuleResponse> getAllRules() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    @Cacheable(value = "rules", key = "#id")
    @Transactional(readOnly = true)
    public AutopayRuleResponse getRuleById(Long id) {
        log.info("Fetching rule by id: {}", id);
        AutopayRuleEntity entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Rule not found: id={}", id);
                    return new RuleNotFoundException(id);
                });
        log.debug("Rule found: {}", entity);
        return mapper.toDto(entity);
    }

    @Override
    @CacheEvict(value = {"rules", "activeRules"}, allEntries = true)
    @Transactional
    public AutopayRuleResponse createRule(AutopayRuleRequest request) {
        log.info("Creating new rule for userId: {}, amount: {}",
                request.getUserId(), request.getAmount());

        AutopayRuleEntity saved = repository.save(mapper.toEntity(request));
        log.info("Rule created with id: {}", saved.getId());

        return mapper.toDto(saved);
    }

    @Override
    @CacheEvict(value = "rules", key = "#id")
    @Transactional
    public AutopayRuleResponse updateRuleById(Long id, AutopayRuleRequest request) {
        AutopayRuleEntity entity = repository.findById(id).orElseThrow(() -> new RuleNotFoundException(id));
        entity.setUserId(request.getUserId());
        entity.setRecipientId(request.getRecipientId());
        entity.setAmount(request.getAmount());
        entity.setCronExpression(request.getCronExpression());
        entity.setEnabled(request.isEnabled());
        return mapper.toDto(entity);
    }

    @Override
    @CacheEvict(value = {"rules", "activeRules"}, allEntries = true)
    @Transactional
    public void deleteRuleById(Long id) {
        AutopayRuleEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));

        if (entity.getDeletedAt() != null) {
            throw  new RuleAlreadyDeletedException(id);
        }

        repository.softDeleteById(id);
    }

    @Override
    @CacheEvict(value = {"rules", "activeRules"}, allEntries = true)
    @Transactional
    public AutopayRuleResponse restoreRuleById(Long id) {
        log.info("Restoring rule with id: {}", id);

        // Проверяем, что правило существует (даже удалённое)
        AutopayRuleEntity entity = repository.findByIdWithDeleted(id)
                .orElseThrow(() -> new RuleNotFoundException(id));

        // Если правило не было удалено — бросаем исключение
        if (entity.getDeletedAt() == null) {
            throw new IllegalStateException("Rule with id " + id + " is not deleted");
        }

        repository.restoreById(id);
        log.info("Rule restored: id={}", id);

        // Получаем обновлённое правило
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id)));
    }
}
