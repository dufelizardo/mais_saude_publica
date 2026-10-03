package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Equipamentos de Saúde (ADR-0101): cadastro por id com a regra de nível, CNES único, rede com contagens, horário
 * estruturado, situação operacional com histórico, e a agenda que respeita o horário e a unidade fechada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedeUnidadesControllerTest {

    private static final String URL = "/api/v1/unidade-saude/";
    private static final String AGENDA = "/api/v1/agenda/";
    private static final String PREFIXO = "Rede Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private UnidadeDeSaude municipio;
    private UnidadeDeSaude estado;

    @BeforeEach
    void seed() {
        limpar();
        estado = gestao("Estado", TipoUnidadeDeSaude.ESTADUAL);
        municipio = gestao("Município", TipoUnidadeDeSaude.MUNICIPAL);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_agendamento where unidade_id = ?", u);
            jdbc.update("delete from tb_bloco_agenda where unidade_id = ?", u);
            jdbc.update("delete from tb_horario_unidade where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_situacao_unidade where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        jdbc.update("update tb_unidade_de_saude set unidade_superior_id = null where nome like ?", PREFIXO + "%");
        unidadeDeSaudeRepository.deleteAllById(unidades);
        profissionalRepository.findByMatricula("REDE-MED").ifPresent(profissionalRepository::delete);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
    }

    private UnidadeDeSaude gestao(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private ResultActions criar(String nome, String tipo, UUID superior, String cnes) throws Exception {
        return mockMvc.perform(post(URL + "id").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "tipo": "%s", "unidadeSuperiorId": %s, "cnes": %s, "email": "unidade@saude.gov.br",
                 "telefones": ["011-3812-4400"], "endereco": {"logradouro": "Rua das Acácias", "numeroLogradouro": "488",
                 "bairro": "Vila Esperança", "cidade": "São Paulo", "estado": "SP", "cep": "04501-200"}}
                """.formatted(PREFIXO + nome, tipo, superior == null ? "null" : "\"" + superior + "\"", cnes == null ? "null" : "\"" + cnes + "\"")));
    }

    private UUID criada(String nome, String tipo, String cnes) throws Exception {
        String corpo = criar(nome, tipo, municipio.getUuid(), cnes).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.resumo.uuid"));
    }

    @Test
    void cadastroPorIdRespeitaNivelCnesUnicoENomeUnico() throws Exception {
        criar("UBS Vila Esperança", "UBS", municipio.getUuid(), "2145089")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumo.situacaoOperacional").value("EM_OPERACAO"))
                .andExpect(jsonPath("$.resumo.funciona24h").value(false))
                .andExpect(jsonPath("$.resumo.unidadeSuperiorNome").value(PREFIXO + "Município"))
                .andExpect(jsonPath("$.logradouro").value("Rua das Acácias"));
        criar("UPA São Pedro", "UPA", municipio.getUuid(), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumo.funciona24h").value(true));
        criar("UBS Errada", "UBS", estado.getUuid(), null).andExpect(status().isUnprocessableEntity());
        criar("UBS Vila Esperança", "UBS", municipio.getUuid(), null).andExpect(status().isConflict());
        criar("UBS Outra", "UBS", municipio.getUuid(), "2145089").andExpect(status().isConflict());
        criar("UBS Cnes Curto", "UBS", municipio.getUuid(), "12345").andExpect(status().isBadRequest());
    }

    @Test
    void redeTrazContagensEIndicadores() throws Exception {
        UUID hospital = criada("Hospital", "HOSPITAL", null);
        UnidadeDeSaude h = unidadeDeSaudeRepository.findById(hospital).orElseThrow();
        Setor clinica = setorRepository.save(new Setor(h, PREFIXO + "Clínica", "RD-CL", TipoSetor.ASSISTENCIAL, true, null));
        for (int i = 1; i <= 2; i++) {
            Leito l = new Leito();
            l.setUnidade(h);
            l.setSetor(clinica);
            l.setIdentificacao("RD " + i);
            l.setTipo(TipoLeito.CLINICO);
            l.setSexo(SexoLeito.MISTO);
            l.setSituacao(i == 1 ? SituacaoLeito.OCUPADO : SituacaoLeito.LIVRE);
            l.setAtivo(true);
            leitoRepository.save(l);
        }
        mockMvc.perform(get(URL + "rede"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rede[?(@.uuid == '%s')].setores".formatted(hospital)).value(1))
                .andExpect(jsonPath("$.rede[?(@.uuid == '%s')].leitos".formatted(hospital)).value(2))
                .andExpect(jsonPath("$.rede[?(@.uuid == '%s')].leitosOcupados".formatted(hospital)).value(1))
                .andExpect(jsonPath("$.porTipo.HOSPITAL", notNullValue()));
        mockMvc.perform(get(URL + "id/" + hospital))
                .andExpect(jsonPath("$.setoresDaUnidade[0].nome").value(PREFIXO + "Clínica"));
        mockMvc.perform(get(URL + "id/" + municipio.getUuid()))
                .andExpect(jsonPath("$.subordinadas[?(@.uuid == '%s')]".formatted(hospital)).isNotEmpty());
        mockMvc.perform(patch(URL + "id/" + hospital).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "tipo": "UBS"}""".formatted(PREFIXO + "Hospital"))).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(URL + "id/" + hospital).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "tipo": "HOSPITAL", "cnes": "7654321"}""".formatted(PREFIXO + "Hospital Geral")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.nome").value(PREFIXO + "Hospital Geral"))
                .andExpect(jsonPath("$.resumo.cnes").value("7654321"));
    }

    @Test
    void horarioEstruturadoESituacaoFechamAAgenda() throws Exception {
        UUID ubs = criada("UBS Centro", "UBS", null);
        String horario = "{\"funciona24h\": false, \"turnos\": [%s]}";
        mockMvc.perform(put(URL + "id/" + ubs + "/horarios").contentType(MediaType.APPLICATION_JSON)
                        .content(horario.formatted("{\"diaSemana\": \"MONDAY\", \"abre\": \"08:00\", \"fecha\": \"12:00\"},"
                                + "{\"diaSemana\": \"MONDAY\", \"abre\": \"11:00\", \"fecha\": \"17:00\"}")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(URL + "id/" + ubs + "/horarios").contentType(MediaType.APPLICATION_JSON)
                        .content(horario.formatted("{\"diaSemana\": \"MONDAY\", \"abre\": \"12:00\", \"fecha\": \"08:00\"}")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(URL + "id/" + ubs + "/horarios").contentType(MediaType.APPLICATION_JSON)
                        .content(horario.formatted("{\"diaSemana\": \"MONDAY\", \"abre\": \"08:00\", \"fecha\": \"12:00\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnos.length()").value(1))
                .andExpect(jsonPath("$.resumo.comHorario").value(true));

        Profissional medica = new Profissional();
        medica.setMatricula("REDE-MED");
        medica.setCpf("71829364511");
        medica.setNome(PREFIXO + "Médica");
        medica.setAtivo(true);
        profissionalRepository.save(medica);
        String bloco = """
                {"profissionalMatricula": "REDE-MED", "unidadeId": "%s", "diaSemana": "MONDAY", "horaInicio": "%s",
                 "horaFim": "%s", "duracaoMinutos": 30, "tipo": "CONSULTA"}""";
        mockMvc.perform(post(AGENDA + "bloco").contentType(MediaType.APPLICATION_JSON).content(bloco.formatted(ubs, "13:00", "15:00")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(AGENDA + "bloco").contentType(MediaType.APPLICATION_JSON).content(bloco.formatted(ubs, "08:00", "10:00")))
                .andExpect(status().isCreated());
        LocalDate segunda = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        String doDia = "?profissionalMatricula=REDE-MED&unidadeId=" + ubs + "&de=" + segunda + "&ate=" + segunda;
        mockMvc.perform(get(AGENDA + doDia)).andExpect(jsonPath("$.vagasOfertadas").value(4));

        // Encaixe fora do horário da unidade é recusado.
        Paciente paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        String marcacao = """
                {"pacienteId": "%s", "profissionalMatricula": "REDE-MED", "dataHora": "%sT%s:00", "status": "AGENDADO",
                 "tipo": "CONSULTA", "unidadeId": "%s", "encaixe": true}""";
        mockMvc.perform(post("/api/v1/agendamento/").contentType(MediaType.APPLICATION_JSON)
                        .content(marcacao.formatted(paciente.getUuid(), segunda, "14:00", ubs)))
                .andExpect(status().isUnprocessableEntity());

        String situacao = "{\"situacao\": \"%s\"%s}";
        mockMvc.perform(post(URL + "id/" + ubs + "/situacao").contentType(MediaType.APPLICATION_JSON).content(situacao.formatted("EM_OBRA", "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(URL + "id/" + ubs + "/situacao").contentType(MediaType.APPLICATION_JSON)
                        .content(situacao.formatted("EM_OBRA", ", \"motivo\": \"Reforma do telhado\", \"previsaoRetorno\": \"" + segunda.plusMonths(2) + "\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.situacaoOperacional").value("EM_OBRA"))
                .andExpect(jsonPath("$.historico.length()").value(1));
        mockMvc.perform(post(URL + "id/" + ubs + "/situacao").contentType(MediaType.APPLICATION_JSON)
                        .content(situacao.formatted("EM_OBRA", ", \"motivo\": \"Reforma do telhado\", \"previsaoRetorno\": \"" + segunda.plusMonths(2) + "\"")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(AGENDA + doDia))
                .andExpect(jsonPath("$.vagasOfertadas").value(0))
                .andExpect(jsonPath("$.avisoUnidade").value(org.hamcrest.Matchers.containsString("obra")));
        mockMvc.perform(post("/api/v1/agendamento/").contentType(MediaType.APPLICATION_JSON)
                        .content(marcacao.formatted(paciente.getUuid(), segunda, "08:00", ubs)))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post(URL + "id/" + ubs + "/situacao").contentType(MediaType.APPLICATION_JSON).content(situacao.formatted("EM_OPERACAO", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.motivoSituacao").doesNotExist())
                .andExpect(jsonPath("$.historico.length()").value(2));
        mockMvc.perform(get(AGENDA + doDia)).andExpect(jsonPath("$.vagasOfertadas").value(4));
    }
}
