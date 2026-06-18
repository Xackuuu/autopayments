package com.bank.autopay;

import com.bank.autopay.dto.AutopayRuleRequest;
import com.bank.autopay.dto.AutopayRuleResponse;
import com.bank.autopay.repository.AutoPayRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class AutopayRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AutoPayRuleRepository repository;
    @Autowired
    private CacheManager cacheManager;
    /**
     * Создание правила
     * @throws Exception
     */
    @Test
    void shouldCreateRule() throws Exception {
        AutopayRuleRequest request = createAutopayRuleRequest();

        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andDo(result -> {
                    log.info("\n✅✅✅ Получен ответ: {}✅✅✅", result.getResponse().getContentAsString());
                });
    }

    /**
     * Некорректная сумма списания
     * @throws Exception
     */
    @Test
    void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
        AutopayRuleRequest request = createAutopayRuleRequest();
        request.setAmount(BigDecimal.ZERO);  // ← Некорректная сумма

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    log.info("\n✅✅✅ Получен ответ: {}✅✅✅", result.getResponse().getContentAsString());
                });
    }
    /**
     * Некорректный крон
     * @throws Exception
     */
    @Test
    void shouldReturnBadRequestWhenCronInvalid() throws Exception {
        AutopayRuleRequest request = createAutopayRuleRequest();
        request.setCronExpression("invalid");  // ← Некорректный cron

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    log.info("\n✅✅✅ Получен ответ: {}✅✅✅", result.getResponse().getContentAsString());
                });
    }

    @Test
    void shouldEvictCacheOnUpdate() throws Exception {
        AutopayRuleRequest request = createAutopayRuleRequest();
        log.info(cacheStats());

        MvcResult result = mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        AutopayRuleResponse created = objectMapper.readValue(responseContent, AutopayRuleResponse.class);
        Long id = created.getId();
        log.info("\n✅✅✅ Создана сущность с ID: {}✅✅✅", id);
        log.info(cacheStats());

        MvcResult resultGet = mockMvc.perform(get("/api/rules/" + id))
                .andExpect(status().isOk())
                .andReturn();
        log.info("\n📝📝📝 Полученна сущность : {}📝📝📝", resultGet.getResponse().getContentAsString());
        log.info(cacheStats());

        request.setAmount(new BigDecimal("9.99"));
        mockMvc.perform(put("/api/rules/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        log.info("\n✅✅✅ Обновленна сущность с ID: {}✅✅✅", id);
        log.info(cacheStats());
    }

    private AutopayRuleRequest createAutopayRuleRequest() {
        AutopayRuleRequest request = new AutopayRuleRequest();
        request.setUserId(5L);
        request.setRecipientId(1L);
        request.setAmount(new BigDecimal("3.50"));
        request.setCronExpression("50 * * * * ?");
        return request;
    }

    private String cacheStats() {
        Cache cache = cacheManager.getCache("rules");
        if (cache instanceof CaffeineCache caffeineCache) {
            return "\n✅✅✅" + caffeineCache.getNativeCache().stats() + "✅✅✅";
        }
        return "\n❌❌❌Cache is null❌❌❌";
    }
}
