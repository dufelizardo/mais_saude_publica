package com.edufelizardo.maissaudepublica.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde para as verificações do Kubernetes (ADR-0145), com login e autorização ligados: o health, a vivacidade e a
 * prontidão respondem sem token e só com o estado; o resto do Actuator não fica exposto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class SaudeActuatorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthLivenessEReadinessSemTokenSoComOEstado() throws Exception {
        for (String rota : new String[]{"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"}) {
            String corpo = mockMvc.perform(get(rota)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                    .andReturn().getResponse().getContentAsString();
            // Sem detalhe: nem componentes, nem banco, nem versão.
            assertThat(corpo).doesNotContain("components").doesNotContain("details").doesNotContain("postgres");
        }
    }

    @Test
    void restoDoActuatorFechado() throws Exception {
        for (String rota : new String[]{"/actuator/env", "/actuator/beans", "/actuator/info"}) {
            int status = mockMvc.perform(get(rota)).andReturn().getResponse().getStatus();
            assertThat(status).as(rota).isNotEqualTo(200);
        }
    }
}
