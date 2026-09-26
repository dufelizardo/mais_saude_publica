package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do MedicamentoController (Farmácia — ver MAPA-DE-DOMINIOS.md #9, ADR-0049). Mesmo padrão
 * de engenharia de teste dos demais controllers raiz (sem FK) da plataforma: SEM
 * {@code @Transactional} na classe, limpeza manual via {@code @AfterEach} por prefixo de nome
 * (mesmo raciocínio do PacienteControllerTest por prefixo de CPF).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicamentoControllerTest {

    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String PREFIXO_NOME_TESTE = "Medicamento Teste - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<Medicamento> criadosNoTeste = medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        medicamentoRepository.deleteAll(criadosNoTeste);
    }

    private String corpoMedicamento(String nome, String principioAtivo, String apresentacao, boolean ativo) {
        return """
                {
                  "nome": "%s",
                  "principioAtivo": "%s",
                  "apresentacao": "%s",
                  "codigo": "COD-%s",
                  "ativo": %s
                }
                """.formatted(nome, principioAtivo, apresentacao, nome.hashCode(), ativo);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        mockMvc.perform(post(MEDICAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoMedicamento(PREFIXO_NOME_TESTE + "Dipirona", "Dipirona Sódica", "Comprimido 500mg", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Medicamento criado com sucesso!"));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(MEDICAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Paracetamol";
        mockMvc.perform(post(MEDICAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoMedicamento(nome, "Paracetamol", "Comprimido 750mg", true)))
                .andExpect(status().isCreated());

        UUID uuid = medicamentoRepository.findAll().stream()
                .filter(m -> nome.equals(m.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(get(MEDICAMENTO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.apresentacao").value("Comprimido 750mg"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(MEDICAMENTO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarApresentacao() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Amoxicilina";
        mockMvc.perform(post(MEDICAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoMedicamento(nome, "Amoxicilina", "Cápsula 500mg", true)))
                .andExpect(status().isCreated());

        UUID uuid = medicamentoRepository.findAll().stream()
                .filter(m -> nome.equals(m.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        String bodyAtualizado = corpoMedicamento(nome, "Amoxicilina", "Suspensão oral 250mg/5mL", false);

        mockMvc.perform(patch(MEDICAMENTO_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizado))
                .andExpect(status().isOk());

        Medicamento atualizado = medicamentoRepository.findById(uuid).orElseThrow();
        assertThat(atualizado.getApresentacao()).isEqualTo("Suspensão oral 250mg/5mL");
        assertThat(atualizado.isAtivo()).isFalse();
    }
}
