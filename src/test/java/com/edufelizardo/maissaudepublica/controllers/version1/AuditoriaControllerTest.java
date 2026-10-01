package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Consulta da trilha de auditoria (ADR-0071), com os toggles desligados — sem login, sem escopo. O escopo
 * por unidade é coberto no {@code AutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaControllerTest {

    private static final String URL = "/api/v1/auditoria/";
    private static final String NOME = "Paciente Auditoria Teste";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        Paciente p = new Paciente();
        p.setNome(NOME);
        p.setAtivo(true);
        // O DTO de paciente exige endereço.
        Endereco endereco = new Endereco();
        endereco.setCep("01001-000");
        endereco.setLogradouro("Praça da Sé");
        endereco.setNumeroLogradouro("1");
        endereco.setBairro("Sé");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        p.setEndereco(endereco);
        paciente = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream().filter(p -> NOME.equals(p.getNome())).toList());
    }

    @Test
    void leituraDoPacienteApareceNaConsultaPorPaciente() throws Exception {
        mockMvc.perform(get("/api/v1/paciente/" + paciente.getUuid())).andExpect(status().isOk());

        mockMvc.perform(get(URL).param("pacienteId", paciente.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].acao").value("LEITURA"))
                .andExpect(jsonPath("$.itens[0].resultado").value("PERMITIDO"))
                .andExpect(jsonPath("$.itens[0].recurso").value("PACIENTE"))
                .andExpect(jsonPath("$.itens[0].rota").value("/api/v1/paciente/{uuid}"))
                .andExpect(jsonPath("$.itens[0].pacienteNome").value(NOME))
                .andExpect(jsonPath("$.resumo.leituras").value(1));

        mockMvc.perform(get(URL).param("pacienteId", paciente.getUuid().toString()).param("acao", "CRIACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.itens.length()").value(0));
    }

    @Test
    void consultarAAuditoriaTambemFicaNaTrilha() throws Exception {
        mockMvc.perform(get(URL).param("tamanho", "5")).andExpect(status().isOk());
        mockMvc.perform(get(URL).param("acao", "LEITURA").param("tamanho", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[?(@.recurso=='AUDITORIA')]").isNotEmpty())
                .andExpect(jsonPath("$.tamanho").value(100))
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void deveRecusarFiltroInvalido() throws Exception {
        mockMvc.perform(get(URL).param("desde", "2026-10-10").param("ate", "2026-10-01")).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL).param("tamanho", "500")).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL).param("acao", "INVALIDA")).andExpect(status().isBadRequest());
    }
}
