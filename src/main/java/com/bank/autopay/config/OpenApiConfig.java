package com.bank.autopay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Autopay System API")
                        .version("1.0.0")
                        .description("""
                                Банковская система автоплатежей.
                                
                                ## Возможности:
                                - Создание правил автоплатежа
                                - Управление правилами (CRUD)
                                - Автоматическое выполнение платежей по расписанию
                                - Мягкое удаление и восстановление
                                - Кеширование для повышения производительности
                                - Мониторинг через Actuator
                                """)
                        .contact(new Contact()
                                .name("Xackuuu")
                                .email("support@bank.com")
                                .url("https://github.com/Xackuuu"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.bank.com")
                                .description("Production server")
                ));
    }
}