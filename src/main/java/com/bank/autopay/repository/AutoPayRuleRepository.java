package com.bank.autopay.repository;

import com.bank.autopay.domain.AutopayRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoPayRuleRepository extends JpaRepository<AutopayRuleEntity, Long> {

    List<AutopayRuleEntity> findByEnabledTrue();

}
