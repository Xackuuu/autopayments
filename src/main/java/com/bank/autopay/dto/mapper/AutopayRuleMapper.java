package com.bank.autopay.dto.mapper;

import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AutopayRuleMapper {

    AutopayRuleEntity toEntity(AutopayRuleRequest request);

    AutopayRuleResponse toDto(AutopayRuleEntity entity);

    List<AutopayRuleResponse> toDtoList(List<AutopayRuleEntity> entityList);
}
