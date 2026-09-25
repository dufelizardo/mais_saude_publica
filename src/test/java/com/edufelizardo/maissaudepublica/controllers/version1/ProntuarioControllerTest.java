package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do ProntuarioController (Assistência — ver ADR-0039/ADR-0045). Endpoint de leitura pura,
 * sem criação própria: os dados vêm de Atendimento/Consulta/Procedimento. Mesmo padrão de limpeza
 * incondicional das fases anteriores desta onda (único teste que cria esses registros).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProntuarioControllerTest {

    private static final String PRONTUARIO_URL = "/api/v1/prontuario/";
    private static final String PROCEDIMENTO_URL = "/api/v1/procedimento/";
    private static final String CONSULTA_URL = "/api/v1/consulta/";
    private static final String ATENDIMENTO_URL = "/api/v1/atendimento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_NOME_TESTE = "Prontuario Teste - ";
    private static final String PREFIXO_CPF_TESTE = "22211100";

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

    private UUID criarAtendimentoEBuscarUuid(UUID pacienteId, String matricula, UUID unidadeId) throws Exception {
        mockMvc.perform(post(ATENDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": "%s",
                                  "profissionalMatricula": "%s",
                                  "unidadeId": "%s",
                                  "tipo": "CONSULTA",
                                  "status": "CONCLUIDO",
                                  "dataHora": "2026-01-01T08:00:00"
                                }
                                """.formatted(pacienteId, matricula, unidadeId)))
                .andExpect(status().isCreated());

        return atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private UUID criarConsultaEBuscarUuid(UUID atendimentoId, String matricula) throws Exception {
        mockMvc.perform(post(CONSULTA_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "atendimentoId": "%s",
                                  "profissionalMatricula": "%s",
                                  "dataHora": "2026-01-01T08:30:00",
                                  "tipoConsulta": "PRIMEIRA",
                                  "diagnostico": "Cefaleia tensional"
                                }
                                """.formatted(atendimentoId, matricula)))
                .andExpect(status().isCreated());

        return consultaRepository.findAll().stream()
                .filter(c -> c.getAtendimento().getUuid().equals(atendimentoId))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private void criarProcedimento(UUID consultaId, String matricula) throws Exception {
        mockMvc.perform(post(PROCEDIMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "consultaId": "%s",
                                  "profissionalMatricula": "%s",
                                  "tipo": "EXAME",
                                  "dataRealizacao": "2026-01-01T09:00:00",
                                  "status": "REALIZADO"
                                }
                                """.formatted(consultaId, matricula)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveAgregarAtendimentosConsultasEProcedimentos() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("01");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "01", "Profissional Um");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "01", "Paciente Um");
        UUID atendimentoId = criarAtendimentoEBuscarUuid(pacienteId, matricula, unidadeId);
        UUID consultaId = criarConsultaEBuscarUuid(atendimentoId, matricula);
        criarProcedimento(consultaId, matricula);

        mockMvc.perform(get(PRONTUARIO_URL + pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteUuid").value(pacienteId.toString()))
                .andExpect(jsonPath("$.pacienteNome").value("Paciente Um"))
                .andExpect(jsonPath("$.atendimentos", hasSize(1)))
                .andExpect(jsonPath("$.atendimentos[0].atendimento.uuid").value(atendimentoId.toString()))
                .andExpect(jsonPath("$.atendimentos[0].consultas", hasSize(1)))
                .andExpect(jsonPath("$.atendimentos[0].consultas[0].consulta.uuid").value(consultaId.toString()))
                .andExpect(jsonPath("$.atendimentos[0].consultas[0].consulta.diagnostico").value("Cefaleia tensional"))
                .andExpect(jsonPath("$.atendimentos[0].consultas[0].procedimentos", hasSize(1)))
                .andExpect(jsonPath("$.atendimentos[0].consultas[0].procedimentos[0].tipo").value("EXAME"));
    }

    @Test
    void deveRetornarListaVaziaQuandoPacienteNaoTemAtendimentos() throws Exception {
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "02", "Paciente Dois");

        mockMvc.perform(get(PRONTUARIO_URL + pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteUuid").value(pacienteId.toString()))
                .andExpect(jsonPath("$.atendimentos", hasSize(0)));
    }

    @Test
    void deveRetornarNotFoundQuandoPacienteNaoExiste() throws Exception {
        mockMvc.perform(get(PRONTUARIO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
