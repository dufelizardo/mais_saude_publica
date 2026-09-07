package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do UnidadeDeSaudeEstadualController (renomeado de HierarquicoUm pela AQUAQE-161,
 * ADR-0009) — mesmas asserções de antes do rename, com o campo `municipio` removido (migrou
 * para Municipal, ver AQUAQE-164) e `estados` renomeado para `estado`. Ver AQUAQE-210.
 *
 * Mesmo padrão de engenharia de teste da AQUAQE-209: SEM {@code @Transactional} na classe,
 * limpeza manual via {@code @AfterEach}, {@code @Transactional} só nos testes de horário.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UnidadeDeSaudeEstadualControllerTest {

    private static final String BASE_URL = "/api/v1/estadual/";
    private static final String PREFIXO_NOME_TESTE = "Secretaria Estadual de Saúde - ";
    private static final String NOME_SUPERIOR = PREFIXO_NOME_TESTE + "Superior";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @BeforeEach
    void criarSuperiorFederal() throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "tipo": "FEDERAL",
                  "endereco": {
                    "cep": "70058-900",
                    "logradouro": "Esplanada dos Ministérios",
                    "numeroLogradouro": "Bloco G",
                    "bairro": "Zona Cívico-Administrativa",
                    "cidade": "Brasília",
                    "estado": "DF"
                  },
                  "email": "contato@saude.gov.br"
                }
                """.formatted(NOME_SUPERIOR);

        mockMvc.perform(post("/api/v1/federal/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void limparDadosDeTeste() {
        List<UnidadeDeSaude> criadosNoTeste = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(criadosNoTeste);
    }

    private void criarUnidadeEstadual(String nome) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "tipo": "ESTADUAL",
                  "administracaoSuperior": "%s",
                  "estado": "SP",
                  "endereco": {
                    "cep": "01037-000",
                    "logradouro": "Rua Conselheiro Crispiniano",
                    "numeroLogradouro": "20",
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-4567-9894"],
                  "email": "contato@saude.sp.gov.br",
                  "horarioFuncionamento": {"MONDAY": "8:00 - 18:00"},
                  "horarioAtendimento": {"MONDAY": "9:00 - 17:00"}
                }
                """.formatted(nome, NOME_SUPERIOR);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Criação";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "ESTADUAL",
                  "administracaoSuperior": "%s",
                  "estado": "SP",
                  "endereco": {
                    "cep": "01037-000",
                    "logradouro": "Rua Conselheiro Crispiniano",
                    "numeroLogradouro": "20",
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@saude.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Unidade de Saúde criada com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)))
                .andExpect(jsonPath("$.details").value(containsString("ESTADUAL")));
    }

    @Test
    void deveRetornarBadRequestAoCriarComNomeDuplicado() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Duplicado";
        criarUnidadeEstadual(nome);

        String body = """
                {
                  "nome": "%s",
                  "tipo": "ESTADUAL",
                  "administracaoSuperior": "%s",
                  "estado": "SP",
                  "endereco": {
                    "cep": "01037-000",
                    "logradouro": "Rua Conselheiro Crispiniano",
                    "numeroLogradouro": "20",
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@saude.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Bad Request"));
    }

    @Test
    void deveRetornarNotFoundAoCriarReferenciandoSuperiorInexistente() throws Exception {
        // Comportamento corrigido pela AQUAQE-13: antes retornava 500, agora 404.
        String body = """
                {
                  "nome": "%sSuperiorInexistente",
                  "tipo": "ESTADUAL",
                  "administracaoSuperior": "NomeQueNaoExisteDeJeitoNenhum-AQUAQE-210",
                  "estado": "SP",
                  "endereco": {
                    "cep": "01037-000",
                    "logradouro": "Rua Conselheiro Crispiniano",
                    "numeroLogradouro": "20",
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@saude.sp.gov.br"
                }
                """.formatted(PREFIXO_NOME_TESTE);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarTodasAsUnidades() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Listagem";
        criarUnidadeEstadual(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItem(nome)));
    }

    @Test
    void deveBuscarPorNome() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Busca";
        criarUnidadeEstadual(nome);

        mockMvc.perform(get(BASE_URL + nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.tipo").value("ESTADUAL"))
                .andExpect(jsonPath("$.estado").value("SP"))
                .andExpect(jsonPath("$.administracaoSuperior").value(NOME_SUPERIOR))
                .andExpect(jsonPath("$.municipio").doesNotExist());
    }

    @Test
    void deveRetornarNotFoundAoBuscarNomeInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + "Nao-Existe-De-Jeito-Nenhum"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarNome() throws Exception {
        String nomeOriginal = PREFIXO_NOME_TESTE + "Antes";
        String nomeNovo = PREFIXO_NOME_TESTE + "Depois";
        criarUnidadeEstadual(nomeOriginal);

        mockMvc.perform(patch(BASE_URL + nomeOriginal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"" + nomeNovo + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").value(containsString(nomeNovo)));

        assertThat(unidadeDeSaudeRepository.findByNome(nomeNovo)).isPresent();
    }

    @Test
    void deveAtualizarContato() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Contato";
        criarUnidadeEstadual(nome);

        String body = """
                {
                  "endereco": {
                    "cep": "01000-000",
                    "logradouro": "Novo Logradouro",
                    "numeroLogradouro": "123",
                    "bairro": "Centro",
                    "cidade": "Campinas",
                    "estado": "SP"
                  },
                  "telefones": ["019-9999-8888"],
                  "email": "novo-contato@saude.sp.gov.br"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "contato/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getEmail()).isEqualTo("novo-contato@saude.sp.gov.br");
        assertThat(atualizado.getEndereco().getCidade()).isEqualTo("Campinas");
    }

    @Test
    @Transactional
    void deveAtualizarHorarioDeFuncionamento() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Horario Funcionamento";
        criarUnidadeEstadual(nome);

        mockMvc.perform(patch(BASE_URL + "horario-de-funcionamento/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horarioFuncionamento\": {\"TUESDAY\": \"7:00 - 19:00\"}}"))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getHorarioFuncionamento()).containsEntry(DayOfWeek.TUESDAY, "7:00 - 19:00");
    }

    @Test
    @Transactional
    void deveAtualizarHorarioDeAtendimento() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Horario Atendimento";
        criarUnidadeEstadual(nome);

        mockMvc.perform(patch(BASE_URL + "horario-de-atendimento/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horarioAtendimento\": {\"WEDNESDAY\": \"10:00 - 16:00\"}}"))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getHorarioAtendimento()).containsEntry(DayOfWeek.WEDNESDAY, "10:00 - 16:00");
    }

    @Test
    void deveDesabilitar_ComportamentoAtual_CorpoDaRequisicaoEIgnorado() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Desabilitar";
        criarUnidadeEstadual(nome);
        assertThat(unidadeDeSaudeRepository.findByNome(nome).orElseThrow().isAtivo()).isTrue();

        // Mesmo comportamento característico documentado na AQUAQE-209: o parâmetro `dto` do
        // endpoint DELETE não tem @RequestBody, então o corpo JSON é ignorado.
        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\": true}"))
                .andExpect(status().isOk());

        assertThat(unidadeDeSaudeRepository.findByNome(nome).orElseThrow().isAtivo()).isFalse();
    }
}
