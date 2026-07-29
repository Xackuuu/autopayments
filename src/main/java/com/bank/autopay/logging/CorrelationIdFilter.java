package com.bank.autopay.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Проверяем, есть ли correlationId в заголовке запроса
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        // 2. Если нет — генерируем новый
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // 3. Кладём в MDC
        MDC.put(CORRELATION_ID_MDC, correlationId);

        // 4. Добавляем в ответ (чтобы клиент знал свой ID)
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            log.info("Request started: method={}, uri={}, correlationId={}",
                    request.getMethod(), request.getRequestURI(), correlationId);
            filterChain.doFilter(request, response);
        } finally {
            log.info("Request completed: method={}, uri={}, correlationId={}",
                    request.getMethod(), request.getRequestURI(), correlationId);
            // 5. Удаляем из MDC
            MDC.clear();
        }
    }
}