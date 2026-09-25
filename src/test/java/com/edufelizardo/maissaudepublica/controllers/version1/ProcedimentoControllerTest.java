package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoRepository;
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
 * Testes do ProcedimentoController (Assistência — ver ADR-0039/ADR-0044). Mesmo padrão de
 * engenharia de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual
 * via {@code @AfterEach} incondicional para Procedimento/Consulta/Atendimento (único teste que os
 * cria). Os helpers de seed devolvem os identificadores necessários (uuid/matricula) em vez de
 * reabrir entidades e navegar associações {@code @ManyToOne LAZY} fora de transação — isso já
 * quebrou o AtendimentoControllerTest e o ConsultaControllerTest, ver seus comentários.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProcedimentoControllerTest {

    private static final String PROCEDIMENTO_URL = "/api/v1/procedimento/";
    private static final String CONSULTA_URL = "/api/v1/consulta/";
    private static final String ATENDIMENTO_URL = "/api/v1/atendimento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_NOME_TESTE = "Procedimento Teste - ";
    private static final String PREFIXO_CPF_TESTE = "33322211";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

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
        // Ordem importa: Procedimento -> Consulta -> Atendimento (cada um referencia o anterior por
        // FK). Nenhum tem campo de teste próprio para filtrar sem navegar associações LAZY fora de
        // transação — este é o único teste que os cria, então apagar tudo é seguro.
        procedimentoRepository.deleteAll();
        consultaRepository.deleteAll();
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

    private record ConsultaSeed(UUID consultaId, String profissionalMatricula) {
    }

    private ConsultaSeed criarConsultaEBuscarUuid(String sufixo) throws Exception {
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

        mockMvc.perform(post(CONSULTA_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atendimentoId": "%s",
                                  "profissionalMatricula": "%s",
                                  "dataHora": "2026-01-01T08:30:00",
                                  "tipoConsulta": "PRIMEIRA"
                                }
                                """.formatted(atendimentoId, matricula)))
                .andExpect(status().isCreated());

        UUID consultaId = consultaRepository.findAll().stream()
                .filter(c -> c.getAtendimento().getUuid().equals(atendimentoId))
                .findFirst()
                .orElseThrow()
                .getUuid();

        return new ConsultaSeed(consultaId, matricula);
    }

    private String corpoProcedimento(UUID consultaId, String profissionalMatricula, String status) {
        return """
                {
                  "consultaId": "%s",
                  "profissionalMatricula": "%s",
                  "tipo": "EXAME",
                  "descricao": "Hemograma completo",
                  "dataRealizacao": "2026-01-01T09:00:00",
                  "status": "%s"
                }
                """.formatted(consultaId, profissionalMatricula, status);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        ConsultaSeed seed = criarConsultaEBuscarUuid("01");

        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProcedimento(seed.consultaId(), seed.profissionalMatricula(), "AGENDADO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Procedimento criado com sucesso!"));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoConsultaNaoExiste() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "02", "Profissional Dois");

        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProcedimento(UUID.randomUUID(), matricula, "AGENDADO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoProfissionalNaoExiste() throws Exception {
        ConsultaSeed seed = criarConsultaEBuscarUuid("03");

        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProcedimento(seed.consultaId(), "00000000000000-00", "AGENDADO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        ConsultaSeed seed = criarConsultaEBuscarUuid("04");

        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProcedimento(seed.consultaId(), seed.profissionalMatricula(), "AGENDADO")))
                .andExpect(status().isCreated());

        UUID uuid = procedimentoRepository.findAll().stream()
                .filter(p -> p.getConsulta().getUuid().equals(seed.consultaId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(get(PROCEDIMENTO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("EXAME"))
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(PROCEDIMENTO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarStatus() throws Exception {
        ConsultaSeed seed = criarConsultaEBuscarUuid("05");

        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProcedimento(seed.consultaId(), seed.profissionalMatricula(), "AGENDADO")))
                .andExpect(status().isCreated());

        UUID uuid = procedimentoRepository.findAll().stream()
                .filter(p -> p.getConsulta().getUuid().equals(seed.consultaId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(patch(PROCEDIMENTO_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoProcedimento(seed.consultaId(), seed.profissionalMatricula(), "REALIZADO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").value("Status: REALIZADO"));

        Procedimento atualizado = procedimentoRepository.findById(uuid).orElseThrow();
        assertThat(atualizado.getStatus().name()).isEqualTo("REALIZADO");
    }
}
