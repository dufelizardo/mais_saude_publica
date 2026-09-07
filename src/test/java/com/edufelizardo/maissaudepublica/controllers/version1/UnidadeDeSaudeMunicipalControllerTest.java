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
 * Testes do UnidadeDeSaudeMunicipalController (renomeado de HierarquicoDois pela AQUAQE-164,
 * ADR-0009) — mesmas asserções de antes do rename, com `regiao` removido (migrou para
 * Regional, ver AQUAQE-167) e `municipio` adicionado (migrado de Estadual, AQUAQE-161).
 * Ver AQUAQE-211.
 *
 * Mesmo padrão de engenharia de teste da AQUAQE-209/210: SEM {@code @Transactional} na classe,
 * limpeza manual via {@code @AfterEach}, {@code @Transactional} só nos testes de horário.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UnidadeDeSaudeMunicipalControllerTest {

    private static final String BASE_URL = "/api/v1/municipal/";
    private static final String PREFIXO_NOME_TESTE = "Secretaria Municipal de Saúde - ";
    private static final String NOME_SUPERIOR_FEDERAL = PREFIXO_NOME_TESTE + "Superior Federal";
    private static final String NOME_SUPERIOR_ESTADUAL = PREFIXO_NOME_TESTE + "Superior Estadual";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @BeforeEach
    void criarCadeiaDeSuperiores() throws Exception {
        String federalBody = """
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
                """.formatted(NOME_SUPERIOR_FEDERAL);

        mockMvc.perform(post("/api/v1/federal/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(federalBody))
                .andExpect(status().isCreated());

        String estadualBody = """
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
                """.formatted(NOME_SUPERIOR_ESTADUAL, NOME_SUPERIOR_FEDERAL);

        mockMvc.perform(post("/api/v1/estadual/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estadualBody))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void limparDadosDeTeste() {
        List<UnidadeDeSaude> criadosNoTeste = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(criadosNoTeste);
    }

    private void criarUnidadeMunicipal(String nome) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "tipo": "MUNICIPAL",
                  "administracaoSuperior": "%s",
                  "municipio": "São Paulo",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-2063-7185"],
                  "email": "contato@prefeitura.sp.gov.br",
                  "horarioFuncionamento": {"MONDAY": "8:00 - 18:00"},
                  "horarioAtendimento": {"MONDAY": "9:00 - 17:00"}
                }
                """.formatted(nome, NOME_SUPERIOR_ESTADUAL);

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
                  "tipo": "MUNICIPAL",
                  "administracaoSuperior": "%s",
                  "municipio": "São Paulo",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@prefeitura.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_ESTADUAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Unidade de Saúde criada com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)))
                .andExpect(jsonPath("$.details").value(containsString("MUNICIPAL")));
    }

    @Test
    void deveRetornarConflictAoCriarComNomeDuplicado() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Duplicado";
        criarUnidadeMunicipal(nome);

        String body = """
                {
                  "nome": "%s",
                  "tipo": "MUNICIPAL",
                  "administracaoSuperior": "%s",
                  "municipio": "São Paulo",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@prefeitura.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_ESTADUAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details").value("Conflict"));
    }

    @Test
    void deveRetornarUnprocessableEntityAoCriarComTipoDivergente() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Tipo Divergente";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "ESTADUAL",
                  "administracaoSuperior": "%s",
                  "municipio": "São Paulo",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@prefeitura.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_ESTADUAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").value("Unprocessable Entity"));
    }

    @Test
    void deveRetornarNotFoundAoCriarReferenciandoSuperiorInexistente() throws Exception {
        // Comportamento corrigido pela AQUAQE-13: antes retornava 500, agora 404.
        String body = """
                {
                  "nome": "%sSuperiorInexistente",
                  "tipo": "MUNICIPAL",
                  "administracaoSuperior": "NomeQueNaoExisteDeJeitoNenhum-AQUAQE-211",
                  "municipio": "São Paulo",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@prefeitura.sp.gov.br"
                }
                """.formatted(PREFIXO_NOME_TESTE);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarUnprocessableEntityAoCriarComSuperiorDeNivelErrado() throws Exception {
        // AQUAQE-22: administracaoSuperior precisa ser do nível ESTADUAL (imediatamente
        // superior a MUNICIPAL) — aqui referenciamos o fixture FEDERAL, inválido.
        String nome = PREFIXO_NOME_TESTE + "Superior Nivel Errado";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "MUNICIPAL",
                  "administracaoSuperior": "%s",
                  "municipio": "São Paulo",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@prefeitura.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_FEDERAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").value("Unprocessable Entity"));
    }

    @Test
    void deveListarTodasAsUnidades() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Listagem";
        criarUnidadeMunicipal(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItem(nome)));
    }

    @Test
    void deveBuscarPorNome() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Busca";
        criarUnidadeMunicipal(nome);

        mockMvc.perform(get(BASE_URL + nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.tipo").value("MUNICIPAL"))
                .andExpect(jsonPath("$.municipio").value("São Paulo"))
                .andExpect(jsonPath("$.administracaoSuperior").value(NOME_SUPERIOR_ESTADUAL))
                .andExpect(jsonPath("$.regiao").doesNotExist());
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
        criarUnidadeMunicipal(nomeOriginal);

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
        criarUnidadeMunicipal(nome);

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
                  "email": "novo-contato@prefeitura.sp.gov.br"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "contato/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getEmail()).isEqualTo("novo-contato@prefeitura.sp.gov.br");
        assertThat(atualizado.getEndereco().getCidade()).isEqualTo("Campinas");
    }

    @Test
    @Transactional
    void deveAtualizarHorarioDeFuncionamento() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Horario Funcionamento";
        criarUnidadeMunicipal(nome);

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
        criarUnidadeMunicipal(nome);

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
        criarUnidadeMunicipal(nome);
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
