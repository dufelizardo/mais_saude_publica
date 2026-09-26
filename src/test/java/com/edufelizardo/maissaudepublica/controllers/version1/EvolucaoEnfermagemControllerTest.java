package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EvolucaoEnfermagemRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
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
 * Testes do EvolucaoEnfermagemController (Enfermagem — ver MAPA-DE-DOMINIOS.md #8, ADR-0048). Mesmo
 * padrão de engenharia de teste dos demais controllers da onda Assistência/Enfermagem: SEM
 * {@code @Transactional} na classe, limpeza manual via {@code @AfterEach} incondicional para
 * EvolucaoEnfermagem/Atendimento (único teste que os cria — mesmo raciocínio do
 * TriagemControllerTest, evita navegar associações LAZY fora de transação).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvolucaoEnfermagemControllerTest {

    private static final String EVOLUCAO_URL = "/api/v1/evolucao-enfermagem/";
    private static final String ATENDIMENTO_URL = "/api/v1/atendimento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_NOME_TESTE = "Evolucao Teste - ";
    private static final String PREFIXO_CPF_TESTE = "66655544";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EvolucaoEnfermagemRepository evolucaoEnfermagemRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @AfterEach
    void limparDadosDeTeste() {
        // EvolucaoEnfermagem/Atendimento não têm campo de teste próprio para filtrar sem navegar
        // associações LAZY fora de transação (mesmo raciocínio do TriagemControllerTest). Este é o
        // único teste que cria EvolucaoEnfermagem, então apagar tudo é seguro. EvolucaoEnfermagem
        // sai primeiro: ela referencia Atendimento, então apagar Atendimento antes seria bloqueado
        // pela FK.
        evolucaoEnfermagemRepository.deleteAll();
        atendimentoRepository.deleteAll();

        List<Paciente> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        pacienteRepository.deleteAll(pacientes);

        List<Profissional> profissionais = profissionalRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        profissionalRepository.deleteAll(profissionais);

        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(unidades);
    }

    private UUID criarUnidadeSaudeUbs(String sufixo) throws Exception {
        String nomeFederal = PREFIXO_NOME_TESTE + "Federal " + sufixo;
        String nomeEstadual = PREFIXO_NOME_TESTE + "Estadual " + sufixo;
        String nomeMunicipal = PREFIXO_NOME_TESTE + "Municipal " + sufixo;
        String nomeUnidade = PREFIXO_NOME_TESTE + "UBS " + sufixo;

        mockMvc.perform(post(FEDERAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                """.formatted(nomeFederal)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ESTADUAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                """.formatted(nomeEstadual, nomeFederal)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(MUNICIPAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                """.formatted(nomeMunicipal, nomeEstadual)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(UNIDADE_SAUDE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                """.formatted(nomeUnidade, nomeMunicipal)))
                .andExpect(status().isCreated());

        return unidadeDeSaudeRepository.findByNome(nomeUnidade).orElseThrow().getUuid();
    }

    private String criarProfissionalEBuscarMatricula(String cpf, String nome) throws Exception {
        mockMvc.perform(post(PROFISSIONAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "%s",
                                  "nome": "%s",
                                  "endereco": {
                                    "cep": "01310-100",
                                    "logradouro": "Avenida Paulista",
                                    "numeroLogradouro": "1000",
                                    "bairro": "Bela Vista",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "telefones": ["011-2063-7185"],
                                  "email": "profissional@saude.sp.gov.br"
                                }
                                """.formatted(cpf, nome)))
                .andExpect(status().isCreated());

        return profissionalRepository.findByCpfAndAtivoTrue(cpf).orElseThrow().getMatricula();
    }

    private UUID criarPacienteEBuscarUuid(String cpf, String nome) throws Exception {
        mockMvc.perform(post(PACIENTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "cpf": "%s",
                                  "dataNascimento": "1990-05-10",
                                  "sexo": "FEMININO",
                                  "endereco": {
                                    "cep": "01310-100",
                                    "logradouro": "Avenida Paulista",
                                    "numeroLogradouro": "1000",
                                    "bairro": "Bela Vista",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "telefones": ["011-2063-7185"],
                                  "email": "paciente@exemplo.com",
                                  "ativo": true
                                }
                                """.formatted(nome, cpf)))
                .andExpect(status().isCreated());

        return pacienteRepository.findByCpf(cpf).stream().findFirst().orElseThrow().getUuid();
    }

    /**
     * Retorna o uuid do Atendimento junto com a matrícula do Profissional vinculado — evitar
     * reabrir a entidade e navegar {@code atendimento.getProfissional()} fora de uma transação
     * (LAZY, lançaria LazyInitializationException).
     */
    private record AtendimentoSeed(UUID atendimentoId, String profissionalMatricula) {
    }

    private AtendimentoSeed criarAtendimentoEBuscarUuid(String sufixo) throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + sufixo, "Profissional " + sufixo);
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + sufixo, "Paciente " + sufixo);

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": "%s",
                                  "profissionalMatricula": "%s",
                                  "unidadeId": "%s",
                                  "tipo": "CONSULTA",
                                  "status": "EM_ANDAMENTO",
                                  "dataHora": "2026-01-01T08:00:00"
                                }
                                """.formatted(pacienteId, matricula, unidadeId)))
                .andExpect(status().isCreated());

        UUID atendimentoId = atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow()
                .getUuid();

        return new AtendimentoSeed(atendimentoId, matricula);
    }

    private String corpoEvolucao(UUID atendimentoId, String profissionalMatricula, String descricao) {
        return """
                {
                  "atendimentoId": "%s",
                  "profissionalMatricula": "%s",
                  "dataHora": "2026-01-01T10:00:00",
                  "descricao": "%s"
                }
                """.formatted(atendimentoId, profissionalMatricula, descricao);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        AtendimentoSeed seed = criarAtendimentoEBuscarUuid("01");

        mockMvc.perform(post(EVOLUCAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEvolucao(seed.atendimentoId(), seed.profissionalMatricula(), "Paciente estável, sem queixas")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Evolução de enfermagem criada com sucesso!"));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(EVOLUCAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoAtendimentoNaoExiste() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "02", "Profissional Dois");

        mockMvc.perform(post(EVOLUCAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEvolucao(UUID.randomUUID(), matricula, "Observação de rotina")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoProfissionalNaoExiste() throws Exception {
        AtendimentoSeed seed = criarAtendimentoEBuscarUuid("03");

        mockMvc.perform(post(EVOLUCAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEvolucao(seed.atendimentoId(), "00000000000000-00", "Observação de rotina")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        AtendimentoSeed seed = criarAtendimentoEBuscarUuid("04");

        mockMvc.perform(post(EVOLUCAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEvolucao(seed.atendimentoId(), seed.profissionalMatricula(), "Sinais vitais estáveis")))
                .andExpect(status().isCreated());

        UUID uuid = evolucaoEnfermagemRepository.findAll().stream()
                .filter(e -> e.getAtendimento().getUuid().equals(seed.atendimentoId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(get(EVOLUCAO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Sinais vitais estáveis"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(EVOLUCAO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarDescricao() throws Exception {
        AtendimentoSeed seed = criarAtendimentoEBuscarUuid("05");

        mockMvc.perform(post(EVOLUCAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoEvolucao(seed.atendimentoId(), seed.profissionalMatricula(), "Descrição inicial")))
                .andExpect(status().isCreated());

        UUID uuid = evolucaoEnfermagemRepository.findAll().stream()
                .filter(e -> e.getAtendimento().getUuid().equals(seed.atendimentoId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        String bodyAtualizado = corpoEvolucao(seed.atendimentoId(), seed.profissionalMatricula(), "Descrição corrigida");

        mockMvc.perform(patch(EVOLUCAO_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyAtualizado))
                .andExpect(status().isOk());

        EvolucaoEnfermagem atualizada = evolucaoEnfermagemRepository.findById(uuid).orElseThrow();
        assertThat(atualizada.getDescricao()).isEqualTo("Descrição corrigida");
    }
}
