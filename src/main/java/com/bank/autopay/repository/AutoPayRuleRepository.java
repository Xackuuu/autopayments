package com.bank.autopay.repository;

import com.bank.autopay.domain.AutopayRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoPayRuleRepository extends JpaRepository<AutopayRuleEntity, Long> {

    /**
     * Поиск активных правил
     * @return
     */
    @Query("SELECT r FROM AutopayRuleEntity r WHERE r.deletedAt IS NULL AND r.enabled = true")
    List<AutopayRuleEntity> findByEnabledTrue();

    @Query("SELECT r FROM AutopayRuleEntity r WHERE r.deletedAt IS NULL")
    List<AutopayRuleEntity> findAll();

    @Query("SELECT r FROM AutopayRuleEntity r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<AutopayRuleEntity> findById(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AutopayRuleEntity r SET r.deletedAt = CURRENT_TIMESTAMP, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AutopayRuleEntity r SET r.deletedAt = NULL, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    void restoreById(@Param("id") Long id);
}
