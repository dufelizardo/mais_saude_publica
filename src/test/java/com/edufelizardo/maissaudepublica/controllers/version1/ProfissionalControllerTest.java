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

import java.time.LocalDate;
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
 * Testes do ProfissionalController (módulo RH — ver ADR-0014/ADR-0017). Mesmo padrão de
 * engenharia de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza
 * manual via {@code @AfterEach} por prefixo de CPF (já que não há um campo "nome de teste"
 * isolado aqui).
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
                  "endereco": {
                    "cep": "01310-100",
                    "logradouro": "Avenida Paulista",
                    "numeroLogradouro": "1000",
                    "bairro": "Bela Vista",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-2063-7185"],
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
                  "endereco": {
                    "cep": "01310-100",
                    "logradouro": "Avenida Paulista",
                    "numeroLogradouro": "1000",
                    "bairro": "Bela Vista",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-2063-7185"],
                  "email": "profissional@saude.sp.gov.br",
                  "dataAdmissao": "2024-01-01"
                }
                """.formatted(cpf, nome);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Profissional criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemEnderecoOuTelefone() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "08";
        String body = """
                {
                  "cpf": "%s",
                  "nome": "Profissional Sem Endereço",
                  "email": "profissional@saude.sp.gov.br"
                }
                """.formatted(cpf);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveCriarComSucesso_CpfRepetidoEmFichaAtiva_NaoEhMaisConflito() throws Exception {
        // CPF não é mais único (ver ADR-0017): uma pessoa pode ter mais de uma ficha (matrículas
        // diferentes) — ex. recontratação. Criar duas fichas com o mesmo CPF deve sempre dar 201.
        String cpf = PREFIXO_CPF_TESTE + "02";
        criarProfissional(cpf, "Profissional Ficha Um");
        criarProfissional(cpf, "Profissional Ficha Dois");
    }

    @Test
    void deveGerarMatriculaUnicaNoFormatoEsperado() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "06";
        String nome = "Profissional Matrícula";
        criarProfissional(cpf, nome);

        Profissional criado = profissionalRepository.findByCpfAndAtivoTrue(cpf).orElseThrow();
        assertThat(criado.getMatricula()).matches("\\d{14}-\\d{2}");
    }

    @Test
    void deveBuscarPorCpf() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "03";
        String nome = "Profissional Busca";
        criarProfissional(cpf, nome);

        mockMvc.perform(get(BASE_URL + cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.telefones[0]").value("011-2063-7185"));
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
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "Campinas",
                    "estado": "SP"
                  },
                  "telefones": ["019-9999-8888"],
                  "email": "novo-contato@saude.sp.gov.br"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "contato/" + cpf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // Verifica via a resposta da API pública, não pelo repositório: `telefones` é uma
        // @ElementCollection LAZY, então reabrir a entidade fora de uma transação/requisição HTTP
        // lançaria LazyInitializationException — o DTO já resolve isso dentro da transação do
        // service.
        mockMvc.perform(get(BASE_URL + cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo-contato@saude.sp.gov.br"))
                .andExpect(jsonPath("$.telefones[0]").value("019-9999-8888"))
                .andExpect(jsonPath("$.endereco.cidade").value("Campinas"));
    }

    @Test
    void deveDesabilitarEGravarDataDeDesligamento() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "05";
        criarProfissional(cpf, "Profissional Desabilitar");
        String matricula = profissionalRepository.findByCpfAndAtivoTrue(cpf).orElseThrow().getMatricula();

        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + cpf)
                        .param("ativo", "false")
                        .param("dataDesligamento", "2026-09-18")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Profissional desabilitado = profissionalRepository.findByMatricula(matricula).orElseThrow();
        assertThat(desabilitado.isAtivo()).isFalse();
        assertThat(desabilitado.getDataDesligamento()).isEqualTo(LocalDate.of(2026, 9, 18));
    }

    @Test
    void deveLimparDataDeDesligamentoAoReabilitar() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "07";
        criarProfissional(cpf, "Profissional Reabilitar");
        String matricula = profissionalRepository.findByCpfAndAtivoTrue(cpf).orElseThrow().getMatricula();

        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + cpf)
                        .param("ativo", "false")
                        .param("dataDesligamento", "2026-09-18")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "des-habilitar/" + cpf)
                        .param("ativo", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Profissional reabilitado = profissionalRepository.findByMatricula(matricula).orElseThrow();
        assertThat(reabilitado.isAtivo()).isTrue();
        assertThat(reabilitado.getDataDesligamento()).isNull();
    }
}
