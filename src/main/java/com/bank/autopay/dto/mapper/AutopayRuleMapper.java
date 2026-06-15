package com.bank.autopay.dto.mapper;

import com.bank.autopay.domain.AutopayRuleEntity;
import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AutopayRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastExecutedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    AutopayRuleEntity toEntity(AutopayRuleRequest request);

    AutopayRuleResponse toDto(AutopayRuleEntity entity);

    List<AutopayRuleResponse> toDtoList(List<AutopayRuleEntity> entityList);
}
