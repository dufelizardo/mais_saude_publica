package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.ReconciliacaoResponsavelScheduler;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prova, ponta a ponta, o mecanismo de vínculo fraco por CPF entre UnidadeDeSaude e Profissional
 * descrito na ADR-0014: uma unidade pode ser cadastrada antes do seu responsável existir no
 * sistema, e o vínculo se resolve depois — seja pela reconciliação síncrona (caso comum), seja
 * pelo job agendado (rede de segurança para condições de corrida ou dados corrigidos
 * manualmente).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReconciliacaoResponsavelTest {

    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PREFIXO_NOME_TESTE = "Reconciliação Teste - ";
    private static final String PREFIXO_CPF_TESTE = "99977766";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ReconciliacaoResponsavelScheduler scheduler;

    @AfterEach
    void limparDadosDeTeste() {
        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(unidades);

        List<Profissional> profissionais = profissionalRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        profissionalRepository.deleteAll(profissionais);
    }

    /**
     * Cria a cadeia completa Federal → Estadual → Municipal exigida por
     * {@code administracaoSuperior} (@NotBlank em cada nível) e retorna o nome do Municipal criado.
     */
    private String criarMunicipal(String sufixo) throws Exception {
        String nomeFederal = PREFIXO_NOME_TESTE + "Federal " + sufixo;
        String nomeEstadual = PREFIXO_NOME_TESTE + "Estadual " + sufixo;
        String nomeMunicipal = PREFIXO_NOME_TESTE + "Municipal " + sufixo;

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
                """.formatted(nomeFederal);

        mockMvc.perform(post(FEDERAL_URL)
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
                """.formatted(nomeEstadual, nomeFederal);

        mockMvc.perform(post(ESTADUAL_URL)
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
                """.formatted(nomeMunicipal, nomeEstadual);

        mockMvc.perform(post(MUNICIPAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(municipalBody))
                .andExpect(status().isCreated());

        return nomeMunicipal;
    }

    private void criarUnidadeSaudeComResponsavelCpf(String nome, String municipal, String cpf) throws Exception {
        String body = """
                {
                  "nome": "%s",
                  "tipo": "UBS",
                  "administracaoSuperior": "%s",
                  "responsavelCpf": "%s",
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
                """.formatted(nome, municipal, cpf);

        mockMvc.perform(post(UNIDADE_SAUDE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private void criarProfissional(String cpf, String nome) throws Exception {
        String body = """
                {
                  "cpf": "%s",
                  "nome": "%s",
                  "email": "profissional@saude.sp.gov.br"
                }
                """.formatted(cpf, nome);

        mockMvc.perform(post(PROFISSIONAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void deveResolverVinculoQuandoProfissionalECadastradoDepoisDaUnidade() throws Exception {
        String municipal = criarMunicipal("Reconciliação 1");
        String nomeUnidade = PREFIXO_NOME_TESTE + "Unidade Pendente";
        String cpf = PREFIXO_CPF_TESTE + "01";
        String nomeProfissional = "Responsável Reconciliado";

        criarUnidadeSaudeComResponsavelCpf(nomeUnidade, municipal, cpf);

        mockMvc.perform(get(UNIDADE_SAUDE_URL + nomeUnidade))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavelCpf").value(cpf))
                .andExpect(jsonPath("$.responsavelNome").doesNotExist());

        criarProfissional(cpf, nomeProfissional);

        mockMvc.perform(get(UNIDADE_SAUDE_URL + nomeUnidade))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsavelNome").value(nomeProfissional));
    }

    @Test
    void jobAgendadoDeveResolverVinculoParaCasosNaoCobertosPelaReconciliacaoSincrona() throws Exception {
        // Simula uma condição de corrida/dado corrigido manualmente: o Profissional é inserido
        // direto no repositório (sem passar pelo ProfissionalService, que dispararia a
        // reconciliação síncrona), então só o job agendado resolve o vínculo.
        String municipal = criarMunicipal("Reconciliação 2");
        String nomeUnidade = PREFIXO_NOME_TESTE + "Unidade Job Agendado";
        String cpf = PREFIXO_CPF_TESTE + "02";

        criarUnidadeSaudeComResponsavelCpf(nomeUnidade, municipal, cpf);

        Profissional profissional = new Profissional();
        profissional.setCpf(cpf);
        profissional.setNome("Responsável via Job");
        profissional.setAtivo(true);
        profissionalRepository.save(profissional);

        UnidadeDeSaude antesDoJob = unidadeDeSaudeRepository.findByNome(nomeUnidade).orElseThrow();
        assertThat(antesDoJob.getResponsavel()).isNull();

        scheduler.reconciliar();

        UnidadeDeSaude depoisDoJob = unidadeDeSaudeRepository.findByNome(nomeUnidade).orElseThrow();
        assertThat(depoisDoJob.getResponsavel()).isNotNull();
        assertThat(depoisDoJob.getResponsavel().getNome()).isEqualTo("Responsável via Job");
    }
}
