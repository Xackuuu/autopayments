package com.bank.autopay;

import com.bank.autopay.dto.AutopayRuleRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class AutopayRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateRule() throws Exception {
        AutopayRuleRequest request = createRequest();

        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void shouldGetAllRules() throws Exception {
        // given
        AutopayRuleRequest request = createRequest();
        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // when/then
        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void shouldReturnNotFoundForNonExistingRule() throws Exception {
        // when/then
        mockMvc.perform(get("/api/rules/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Rule by id 999 not found"));
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
        // given
        AutopayRuleRequest request = createRequest();
        request.setAmount(BigDecimal.ZERO);

        // when/then
        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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