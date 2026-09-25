package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
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
 * Testes do AtendimentoController (Assistência — ver ADR-0039/ADR-0041). Mesmo padrão de
 * engenharia de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual
 * via {@code @AfterEach} por prefixo de nome/cpf.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AtendimentoControllerTest {

    private static final String ATENDIMENTO_URL = "/api/v1/atendimento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_NOME_TESTE = "Atendimento Teste - ";
    private static final String PREFIXO_CPF_TESTE = "66655544";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SetorRepository setorRepository;

    @AfterEach
    void limparDadosDeTeste() {
        // Atendimento não tem um campo de teste próprio para filtrar (paciente/profissional são
        // @ManyToOne LAZY — navegar até paciente.getCpf() fora de uma transação lançaria
        // LazyInitializationException). Este é o único teste que cria Atendimento, então apagar
        // tudo é seguro. Agendamento primeiro seria bloqueado pela FK de tb_atendimento.
        atendimentoRepository.deleteAll();
        agendamentoRepository.deleteAll();

        setorRepository.deleteAll(setorRepository.findAll().stream()
                .filter(s -> s.getNome() != null && s.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList());

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

    private String corpoAtendimento(UUID pacienteId, String profissionalMatricula, UUID unidadeId, UUID setorId,
                                     String tipo, String status) {
        return corpoAtendimento(pacienteId, profissionalMatricula, unidadeId, setorId, null, tipo, status);
    }

    private String corpoAtendimento(UUID pacienteId, String profissionalMatricula, UUID unidadeId, UUID setorId,
                                     UUID agendamentoId, String tipo, String status) {
        String setorJson = setorId == null ? "null" : "\"" + setorId + "\"";
        String agendamentoJson = agendamentoId == null ? "null" : "\"" + agendamentoId + "\"";
        return """
                {
                  "pacienteId": "%s",
                  "profissionalMatricula": "%s",
                  "unidadeId": "%s",
                  "setorId": %s,
                  "agendamentoId": %s,
                  "tipo": "%s",
                  "status": "%s",
                  "dataHora": "2026-01-01T08:00:00"
                }
                """.formatted(pacienteId, profissionalMatricula, unidadeId, setorJson, agendamentoJson, tipo, status);
    }

    private UUID seedAtendimentoUuid(String sufixo) throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + sufixo, "Profissional " + sufixo);
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + sufixo, "Paciente " + sufixo);

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, matricula, unidadeId, null, "CONSULTA", "AGENDADO")))
                .andExpect(status().isCreated());

        Atendimento atendimento = atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow();
        return atendimento.getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("01");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "01", "Profissional Um");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "01", "Paciente Um");

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, matricula, unidadeId, null, "CONSULTA", "AGENDADO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Atendimento criado com sucesso!"));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoPacienteNaoExiste() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("02");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "02", "Profissional Dois");

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(UUID.randomUUID(), matricula, unidadeId, null, "CONSULTA", "AGENDADO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoProfissionalNaoExiste() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("03");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "03", "Paciente Três");

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, "00000000000000-00", unidadeId, null, "CONSULTA", "AGENDADO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCriarComSetorOpcional() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("04");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "04", "Profissional Quatro");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "04", "Paciente Quatro");

        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(unidadeId).orElseThrow();
        Setor setor = new Setor(unidade, PREFIXO_NOME_TESTE + "Setor 04", "COD-04", TipoSetor.ASSISTENCIAL, true, null);
        setor = setorRepository.save(setor);

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, matricula, unidadeId, setor.getUuid(), "CONSULTA", "AGENDADO")))
                .andExpect(status().isCreated());

        Atendimento criado = atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get(ATENDIMENTO_URL + criado.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setorNome").value(PREFIXO_NOME_TESTE + "Setor 04"));
    }

    @Test
    void deveCriarComAgendamentoOpcional() throws Exception {
        // Vínculo acrescentado nesta fase (ADR-0042): um atendimento pode nascer de um agendamento
        // prévio.
        UUID unidadeId = criarUnidadeSaudeUbs("07");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "07", "Profissional Sete");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "07", "Paciente Sete");

        Paciente paciente = pacienteRepository.findById(pacienteId).orElseThrow();
        Profissional profissional = profissionalRepository.findByMatricula(matricula).orElseThrow();
        Agendamento agendamento = new Agendamento(paciente, profissional,
                java.time.LocalDateTime.parse("2026-01-01T08:00:00"), StatusAgendamento.CONFIRMADO,
                TipoAgendamento.CONSULTA, null);
        agendamento = agendamentoRepository.save(agendamento);

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, matricula, unidadeId, null, agendamento.getUuid(),
                                "CONSULTA", "EM_ANDAMENTO")))
                .andExpect(status().isCreated());

        Atendimento criado = atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get(ATENDIMENTO_URL + criado.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agendamentoUuid").value(agendamento.getUuid().toString()));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        UUID uuid = seedAtendimentoUuid("05");

        mockMvc.perform(get(ATENDIMENTO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CONSULTA"))
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(ATENDIMENTO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarStatus() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("06");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "06", "Profissional Seis");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "06", "Paciente Seis");

        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, matricula, unidadeId, null, "CONSULTA", "AGENDADO")))
                .andExpect(status().isCreated());

        UUID uuid = atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(patch(ATENDIMENTO_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAtendimento(pacienteId, matricula, unidadeId, null, "CONSULTA", "CONCLUIDO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").value("Status: CONCLUIDO"));

        Atendimento atualizado = atendimentoRepository.findById(uuid).orElseThrow();
        assertThat(atualizado.getStatus().name()).isEqualTo("CONCLUIDO");
    }
}
