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
 * Testes do UnidadeDeSaudeUnidadeController (5º nível — UBS/HOSPITAL, ver ADR-0013), mesmo
 * padrão de engenharia de teste do UnidadeDeSaudeRegionalControllerTest: SEM
 * {@code @Transactional} na classe, limpeza manual via {@code @AfterEach}, {@code @Transactional}
 * só nos testes de horário.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UnidadeDeSaudeUnidadeControllerTest {

    private static final String BASE_URL = "/api/v1/unidade-saude/";
    private static final String PREFIXO_NOME_TESTE = "Unidade de Saúde Teste - ";
    private static final String NOME_SUPERIOR_FEDERAL = PREFIXO_NOME_TESTE + "Superior Federal";
    private static final String NOME_SUPERIOR_ESTADUAL = PREFIXO_NOME_TESTE + "Superior Estadual";
    private static final String NOME_SUPERIOR_MUNICIPAL = PREFIXO_NOME_TESTE + "Superior Municipal";
    private static final String NOME_REGIONAL = PREFIXO_NOME_TESTE + "Regional de Supervisão";

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

        String municipalBody = """
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
                """.formatted(NOME_SUPERIOR_MUNICIPAL, NOME_SUPERIOR_ESTADUAL);

        mockMvc.perform(post("/api/v1/municipal/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(municipalBody))
                .andExpect(status().isCreated());

        String regionalBody = """
                {
                  "nome": "%s",
                  "tipo": "REGIONAL",
                  "administracaoSuperior": "%s",
                  "regiao": "Sudeste",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@saude.regional.sp.gov.br"
                }
                """.formatted(NOME_REGIONAL, NOME_SUPERIOR_MUNICIPAL);

        mockMvc.perform(post("/api/v1/regional/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regionalBody))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void limparDadosDeTeste() {
        List<UnidadeDeSaude> criadosNoTeste = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(criadosNoTeste);
    }

    private void criarUnidadeSaude(String nome, String tipo) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "tipo": "%s",
                  "administracaoSuperior": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-2063-7185"],
                  "email": "contato@ubs.sp.gov.br",
                  "horarioFuncionamento": {"MONDAY": "8:00 - 18:00"},
                  "horarioAtendimento": {"MONDAY": "9:00 - 17:00"}
                }
                """.formatted(nome, tipo, NOME_SUPERIOR_MUNICIPAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void deveCriarComSucesso_UBS() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Criação UBS";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "UBS",
                  "administracaoSuperior": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@ubs.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_MUNICIPAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Unidade de Saúde criada com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)))
                .andExpect(jsonPath("$.details").value(containsString("UBS")));
    }

    @Test
    void deveCriarComSucesso_HOSPITAL() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Criação Hospital";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "HOSPITAL",
                  "administracaoSuperior": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@hospital.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_MUNICIPAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details").value(containsString("HOSPITAL")));
    }

    @Test
    void deveRetornarConflictAoCriarComNomeDuplicado() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Duplicado";
        criarUnidadeSaude(nome, "UBS");

        String body = """
                {
                  "nome": "%s",
                  "tipo": "UBS",
                  "administracaoSuperior": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@ubs.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_MUNICIPAL);

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
                  "tipo": "MUNICIPAL",
                  "administracaoSuperior": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@ubs.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_MUNICIPAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").value("Unprocessable Entity"));
    }

    @Test
    void deveRetornarNotFoundAoCriarReferenciandoSuperiorInexistente() throws Exception {
        String body = """
                {
                  "nome": "%sSuperiorInexistente",
                  "tipo": "UBS",
                  "administracaoSuperior": "NomeQueNaoExisteDeJeitoNenhum-ADR-0013",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@ubs.sp.gov.br"
                }
                """.formatted(PREFIXO_NOME_TESTE);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarUnprocessableEntityAoCriarComSuperiorDeNivelErrado() throws Exception {
        // A administracaoSuperior de Unidade de Saúde precisa ser do nível MUNICIPAL — aqui
        // referenciamos o fixture ESTADUAL, inválido (ver ADR-0013).
        String nome = PREFIXO_NOME_TESTE + "Superior Nivel Errado";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "UBS",
                  "administracaoSuperior": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@ubs.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_ESTADUAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").value("Unprocessable Entity"));
    }

    @Test
    void deveListarTodasAsUnidades() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Listagem";
        criarUnidadeSaude(nome, "UBS");

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(hasItem(nome)));
    }

    @Test
    void deveBuscarPorNome() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Busca";
        criarUnidadeSaude(nome, "HOSPITAL");

        mockMvc.perform(get(BASE_URL + nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.tipo").value("HOSPITAL"))
                .andExpect(jsonPath("$.administracaoSuperior").value(NOME_SUPERIOR_MUNICIPAL))
                .andExpect(jsonPath("$.supervisaoRegional").doesNotExist())
                .andExpect(jsonPath("$.responsavelNome").doesNotExist());
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
        criarUnidadeSaude(nomeOriginal, "UBS");

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
        criarUnidadeSaude(nome, "UBS");

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
                  "email": "novo-contato@ubs.sp.gov.br"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "contato/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getEmail()).isEqualTo("novo-contato@ubs.sp.gov.br");
        assertThat(atualizado.getEndereco().getCidade()).isEqualTo("Campinas");
    }

    @Test
    @Transactional
    void deveAtualizarHorarioDeFuncionamento() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Horario Funcionamento";
        criarUnidadeSaude(nome, "UBS");

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
        criarUnidadeSaude(nome, "UBS");

        mockMvc.perform(patch(BASE_URL + "horario-de-atendimento/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horarioAtendimento\": {\"WEDNESDAY\": \"10:00 - 16:00\"}}"))
                .andExpect(status().isOk());

        UnidadeDeSaude atualizado = unidadeDeSaudeRepository.findByNome(nome).orElseThrow();
        assertThat(atualizado.getHorarioAtendimento()).containsEntry(DayOfWeek.WEDNESDAY, "10:00 - 16:00");
    }

    @Test
    void deveDesabilitar() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Desabilitar";
        criarUnidadeSaude(nome, "UBS");
        assertThat(unidadeDeSaudeRepository.findByNome(nome).orElseThrow().isAtivo()).isTrue();

        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\": true}"))
                .andExpect(status().isOk());

        assertThat(unidadeDeSaudeRepository.findByNome(nome).orElseThrow().isAtivo()).isFalse();
    }

    @Test
    void deveVincularSupervisaoRegionalNaCriacao() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Com Supervisão na Criação";
        String body = """
                {
                  "nome": "%s",
                  "tipo": "UBS",
                  "administracaoSuperior": "%s",
                  "supervisaoRegional": "%s",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "email": "contato@ubs.sp.gov.br"
                }
                """.formatted(nome, NOME_SUPERIOR_MUNICIPAL, NOME_REGIONAL);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Verifica via a resposta da API pública (não pelo repositório): `supervisaoRegional` é
        // @ManyToOne LAZY, então acessar o proxy fora da transação do service lançaria
        // LazyInitializationException — o DTO já resolve isso dentro da transação do create().
        mockMvc.perform(get(BASE_URL + nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervisaoRegional").value(NOME_REGIONAL));
    }

    @Test
    void deveAtualizarSupervisaoRegionalViaPatch() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Supervisão via Patch";
        criarUnidadeSaude(nome, "UBS");

        mockMvc.perform(patch(BASE_URL + "supervisao-regional/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supervisaoRegional\": \"" + NOME_REGIONAL + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + nome))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervisaoRegional").value(NOME_REGIONAL));
    }

    @Test
    void deveRetornarUnprocessableEntityAoVincularSupervisaoRegionalDeNivelErrado() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Supervisão Nível Errado";
        criarUnidadeSaude(nome, "UBS");

        mockMvc.perform(patch(BASE_URL + "supervisao-regional/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supervisaoRegional\": \"" + NOME_SUPERIOR_MUNICIPAL + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").value("Unprocessable Entity"));
    }

    @Test
    void deveRetornarNotFoundAoVincularSupervisaoRegionalInexistente() throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Supervisão Inexistente";
        criarUnidadeSaude(nome, "UBS");

        mockMvc.perform(patch(BASE_URL + "supervisao-regional/" + nome)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supervisaoRegional\": \"NomeQueNaoExisteDeJeitoNenhum-ADR-0013\"}"))
                .andExpect(status().isNotFound());
    }
}
