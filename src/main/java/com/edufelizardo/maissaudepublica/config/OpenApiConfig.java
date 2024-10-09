package com.edufelizardo.maissaudepublica.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mais Saúde Pública")
                        .version("v1")
                        .description("API para gerenciar a Saúde Pública no Brasil.")
                        .termsOfService("https://github.com/dufelizardo/mais_saude_publica")
                        .license(new License().name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                                .contact(new Contact()
                                        .name("Eduardo Felizardo Cândido")
                                        .url("https://github.com/dufelizardo"))
                        );
    }
}