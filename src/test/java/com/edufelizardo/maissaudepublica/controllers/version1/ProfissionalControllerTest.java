package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do ProfissionalController (módulo RH — ver ADR-0014). Mesmo padrão de engenharia de
 * teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual via
 * {@code @AfterEach} por prefixo de CPF (já que não há um campo "nome de teste" isolado aqui).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfissionalControllerTest {

    private static final String BASE_URL = "/api/v1/profissional/";
    private static final String PREFIXO_CPF_TESTE = "99988877";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<Profissional> criadosNoTeste = profissionalRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        profissionalRepository.deleteAll(criadosNoTeste);
    }

    private void criarProfissional(String cpf, String nome) throws Exception {
        String body = """
                {
                  "cpf": "%s",
                  "nome": "%s",
                  "conselhoClasse": "CRM",
                  "numeroConselho": "123456",
                  "telefone": "011-2063-7185",
                  "email": "profissional@saude.sp.gov.br",
                  "dataAdmissao": "2024-01-01"
                }
                """.formatted(cpf, nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "01";
        String nome = "Profissional Criação";
        String body = """
                {
                  "cpf": "%s",
                  "nome": "%s",
                  "conselhoClasse": "CRM",
                  "numeroConselho": "123456",
                  "telefone": "011-2063-7185",
                  "email": "profissional@saude.sp.gov.br",
                  "dataAdmissao": "2024-01-01"
                }
                """.formatted(cpf, nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Profissional criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)))
                .andExpect(jsonPath("$.details").value(containsString(cpf)));
    }

    @Test
    void deveRetornarConflictAoCriarComCpfDuplicado() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "02";
        criarProfissional(cpf, "Profissional Duplicado");

        String body = """
                {
                  "cpf": "%s",
                  "nome": "Outro Nome",
                  "email": "profissional@saude.sp.gov.br"
                }
                """.formatted(cpf);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details").value("Conflict"));
    }

    @Test
    void deveBuscarPorCpf() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "03";
        String nome = "Profissional Busca";
        criarProfissional(cpf, nome);

        mockMvc.perform(get(BASE_URL + cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.nome").value(nome));
    }

    @Test
    void deveRetornarNotFoundAoBuscarCpfInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + "00000000000-nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarContato() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "04";
        criarProfissional(cpf, "Profissional Contato");

        String body = """
                {
                  "telefone": "019-9999-8888",
                  "email": "novo-contato@saude.sp.gov.br"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "contato/" + cpf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Profissional atualizado = profissionalRepository.findByCpf(cpf).orElseThrow();
        assertThat(atualizado.getEmail()).isEqualTo("novo-contato@saude.sp.gov.br");
        assertThat(atualizado.getTelefone()).isEqualTo("019-9999-8888");
    }

    @Test
    void deveDesabilitar() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "05";
        criarProfissional(cpf, "Profissional Desabilitar");
        assertThat(profissionalRepository.findByCpf(cpf).orElseThrow().isAtivo()).isTrue();

        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + cpf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\": true}"))
                .andExpect(status().isOk());

        assertThat(profissionalRepository.findByCpf(cpf).orElseThrow().isAtivo()).isFalse();
    }
}
