package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
 * Testes do AgendamentoController (Assistência — ver ADR-0039/ADR-0042). Mesmo padrão de
 * engenharia de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual
 * via {@code @AfterEach} incondicional (único teste que cria Agendamento — mesmo raciocínio do
 * AtendimentoControllerTest, evita navegar associações LAZY fora de transação).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoControllerTest {

    private static final String AGENDAMENTO_URL = "/api/v1/agendamento/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_CPF_TESTE = "55544433";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @AfterEach
    void limparDadosDeTeste() {
        agendamentoRepository.deleteAll();

        List<Paciente> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        pacienteRepository.deleteAll(pacientes);

        List<Profissional> profissionais = profissionalRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        profissionalRepository.deleteAll(profissionais);
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

    private String corpoAgendamento(UUID pacienteId, String profissionalMatricula, String status, String tipo) {
        return """
                {
                  "pacienteId": "%s",
                  "profissionalMatricula": "%s",
                  "dataHora": "2026-01-01T08:00:00",
                  "status": "%s",
                  "tipo": "%s",
                  "observacao": "Observação de teste"
                }
                """.formatted(pacienteId, profissionalMatricula, status, tipo);
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "01", "Profissional Um");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "01", "Paciente Um");

        mockMvc.perform(post(AGENDAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(pacienteId, matricula, "AGENDADO", "CONSULTA")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Agendamento criado com sucesso!"));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(AGENDAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoPacienteNaoExiste() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "02", "Profissional Dois");

        mockMvc.perform(post(AGENDAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(UUID.randomUUID(), matricula, "AGENDADO", "CONSULTA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarNotFoundQuandoProfissionalNaoExiste() throws Exception {
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "03", "Paciente Três");

        mockMvc.perform(post(AGENDAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(pacienteId, "00000000000000-00", "AGENDADO", "CONSULTA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "04", "Profissional Quatro");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "04", "Paciente Quatro");

        mockMvc.perform(post(AGENDAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(pacienteId, matricula, "AGENDADO", "CONSULTA")))
                .andExpect(status().isCreated());

        UUID uuid = agendamentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(get(AGENDAMENTO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CONSULTA"))
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(AGENDAMENTO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarStatus() throws Exception {
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "05", "Profissional Cinco");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "05", "Paciente Cinco");

        mockMvc.perform(post(AGENDAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(pacienteId, matricula, "AGENDADO", "CONSULTA")))
                .andExpect(status().isCreated());

        UUID uuid = agendamentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(patch(AGENDAMENTO_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(pacienteId, matricula, "CONFIRMADO", "CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").value("Status: CONFIRMADO"));

        Agendamento atualizado = agendamentoRepository.findById(uuid).orElseThrow();
        assertThat(atualizado.getStatus().name()).isEqualTo("CONFIRMADO");
    }
}
