package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
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
 * Testes do UnidadeDeSaudeFederalController (renomeado de HierarquicoZero pela AQUAQE-158,
 * ADR-0009) — mesmas asserções de antes do rename, só nomes/endpoint atualizados. Ver AQUAQE-209.
 *
 * Propositalmente SEM {@code @Transactional} na classe: cada chamada HTTP via MockMvc precisa
 * rodar (e efetivamente commitar) sua própria transação, igual ao comportamento real em
 * produção — envolver a classe inteira numa única transação de teste distorce cenários como
 * detecção de nome duplicado (o segundo INSERT deixa de "ver" o primeiro a tempo). A limpeza
 * dos dados de teste é feita manualmente no {@code @AfterEach}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UnidadeDeSaudeFederalControllerTest {

    private static final String BASE_URL = "/api/v1/federal/";
    private static final String PREFIXO_NOME_TESTE = "Ministério da Saúde - ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<UnidadeDeSaude> criadosNoTeste = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(criadosNoTeste);
    }

    private void criarUnidadeFederal(String nome) throws Exception {
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
                  "telefones": ["061-4567-9894"],
                  "email": "contato@saude.gov.br",
                  "horarioFuncionamento": {"MONDAY": "8:00 - 18:00"},
                  "horarioAtendimento": {"MONDAY": "9:00 - 17:00"}
                }
                """.formatted(nome);

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
                  "tipo": "FEDERAL",
                  "endereco": {
                    "cep": "70058-900",
                    "logradouro": "Esplanada dos Ministérios",
                    "numeroLogradouro": "Bloco G",
                    "bairro": "Zona Cívico-Administrativa",
                    "cidade": "Brasília",
                    "estado": "DF"
                  },
                  "telefones": ["061-4567-9894"],
                  "email": "contato@saude.gov.br",
                  "horarioFuncionamento": {"MONDAY": "8:00 - 18:00"},
                  "horarioAtendimento": {"MONDAY": "9:00 - 17:00"}
                }
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Unidade de Saúde criada com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)))
                .andExpect(jsonPath("$.details").value(containsString("FEDERAL")));
    }

    @Test
    void deveRetornarBadRequestAoCriarComNomeDuplicado() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Duplicado";
        criarUnidadeFederal(nome);

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
                """.formatted(nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Bad Request"));
    }

    @Test
    void deveListarTodasAsUnidades() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Listagem";
        criarUnidadeFederal(nome);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItem(nome)));
    }

    @Test
    void deveBuscarPorNome() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Busca";
        criarUnidadeFederal(nome);

        mockMvc.perform(get(BASE_URL + nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.tipo").value("FEDERAL"));
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
        criarUnidadeFederal(nomeOriginal);

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
        criarUnidadeFederal(nome);

        String body = """
                {
                  "endereco": {
                    "cep": "01000-000",
                    "logradouro": "Novo Logradouro",
                    "numeroLogradouro": "123",
                    "bairro": "Centro",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-9999-8888"],
                  "email": "novo-contato@saude.gov.br"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "contato/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getEmail()).isEqualTo("novo-contato@saude.gov.br");
        assertThat(atualizado.getEndereco().getCidade()).isEqualTo("São Paulo");
    }

    @Test
    @Transactional
    // @Transactional só neste método: mantém a sessão do Hibernate aberta até a asserção
    // final, necessário porque `horarioFuncionamento` é uma coleção LAZY (@ElementCollection)
    // e o MockMvc já fechou a sessão da requisição quando o teste tenta ler o valor de volta.
    // Não reintroduz o problema visto no teste de nome duplicado, pois aqui não há duas
    // operações concorrentes de escrita dependendo de constraint do banco.
    void deveAtualizarHorarioDeFuncionamento() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Horario Funcionamento";
        criarUnidadeFederal(nome);

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
        criarUnidadeFederal(nome);

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
        criarUnidadeFederal(nome);
        assertThat(unidadeDeSaudeRepository.findByNome(nome).orElseThrow().isAtivo()).isTrue();

        // Nota (característico, confirmado manualmente em 2026-09-07): o parâmetro `dto` do
        // endpoint DELETE não é anotado com @RequestBody, então o Spring MVC o resolve como
        // @ModelAttribute (não lê o corpo JSON) — o valor de "ativo" enviado no body é sempre
        // ignorado, e o campo acaba sempre virando `false` (default do primitivo), mesmo
        // enviando "ativo": true.
        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\": true}"))
                .andExpect(status().isOk());

        assertThat(unidadeDeSaudeRepository.findByNome(nome).orElseThrow().isAtivo()).isFalse();
    }
}
