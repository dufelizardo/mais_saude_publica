package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Situação operacional, fatia BI2 (ADR-0144): a chegada gravada na transição, a fila agora, o tempo de espera com média,
 * mediana e p90, a ocupação igual à da Agenda, as ocorrências da escala (licença médica sem o motivo) e os alertas, com
 * o limite da fila baixado para o teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.indicadores.alertas.fila-aguardando=1"})
class SituacaoOperacionalControllerTest {

    private static final String PREFIXO = "Situacao Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;

    private UnidadeDeSaude ubs;
    private Profissional medico;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        medico = profissional("P-BI2-MED", "90066600001", PREFIXO + "Médico");
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String atendimentos = "(select uuid from tb_atendimento where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_triagem where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_consulta where atendimento_id in " + atendimentos);
        jdbc.update("delete from tb_atendimento where unidade_id in " + unidades);
        jdbc.update("delete from tb_agendamento where unidade_id in " + unidades);
        jdbc.update("delete from tb_bloco_agenda where unidade_id in " + unidades);
        jdbc.update("delete from tb_turno_escala where unidade_id in " + unidades);
        jdbc.update("delete from tb_afastamento where profissional_id in (select uuid from tb_profissional where matricula like 'P-BI2-%')");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'P-BI2-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private Profissional profissional(String matricula, String cpf, String nome) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(nome);
        p.setConselhoClasse("CRM");
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private Paciente paciente() {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + pacientes);
        p.setCpf(String.format("900666%05d", pacientes));
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1990, 1, 1));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    /** Atendimento com a chegada informada (minutos atrás); triagem e consulta em minutos depois da chegada (nulo = sem). */
    private void atendimento(StatusAtendimento status, long chegouHaMin, Long triagemApos, Long consultaApos) {
        LocalDateTime chegada = LocalDateTime.now().minusMinutes(chegouHaMin);
        Atendimento a = new Atendimento(paciente(), medico, ubs, null, null, TipoAtendimento.CONSULTA, status, chegada);
        a.setChegadaEm(chegada);
        a = atendimentoRepository.save(a);
        if (triagemApos != null) {
            jdbc.update("insert into tb_triagem (uuid, classificacao_risco, data_hora, atendimento_id, profissional_id) values (gen_random_uuid(), 'VERDE', ?, ?, ?)",
                    Timestamp.valueOf(chegada.plusMinutes(triagemApos)), a.getUuid(), medico.getUuid());
        }
        if (consultaApos != null) {
            jdbc.update("insert into tb_consulta (uuid, data_hora, atendimento_id, profissional_id) values (gen_random_uuid(), ?, ?, ?)",
                    Timestamp.valueOf(chegada.plusMinutes(consultaApos)), a.getUuid(), medico.getUuid());
        }
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private Timestamp chegadaDe(Paciente p) {
        return jdbc.queryForObject("select chegada_em from tb_atendimento where paciente_id = ?", Timestamp.class, p.getUuid());
    }

    @Test
    void chegadaGravadaNaPrimeiraVezEmAndamento() throws Exception {
        String corpo = "{\"pacienteId\": \"%s\", \"profissionalMatricula\": \"P-BI2-MED\", \"unidadeId\": \"%s\", \"tipo\": \"CONSULTA\", "
                + "\"status\": \"%s\", \"dataHora\": \"%s\"}";
        String hora = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES).toString();
        Paciente direto = paciente();
        enviar(post("/api/v1/atendimento/"), corpo.formatted(direto.getUuid(), ubs.getUuid(), "EM_ANDAMENTO", hora), 201);
        assertThat(chegadaDe(direto)).isNotNull();

        Paciente agendado = paciente();
        enviar(post("/api/v1/atendimento/"), corpo.formatted(agendado.getUuid(), ubs.getUuid(), "AGENDADO", hora), 201);
        assertThat(chegadaDe(agendado)).isNull();
        String id = jdbc.queryForObject("select cast(uuid as varchar) from tb_atendimento where paciente_id = ?", String.class, agendado.getUuid());
        enviar(patch("/api/v1/atendimento/" + id), corpo.formatted(agendado.getUuid(), ubs.getUuid(), "EM_ANDAMENTO", hora), 200);
        Timestamp chegou = chegadaDe(agendado);
        assertThat(chegou).isNotNull();
        // Concluir não muda a chegada.
        enviar(patch("/api/v1/atendimento/" + id), corpo.formatted(agendado.getUuid(), ubs.getUuid(), "CONCLUIDO", hora), 200);
        assertThat(chegadaDe(agendado)).isEqualTo(chegou);

        Paciente retroativo = paciente();
        enviar(post("/api/v1/atendimento/"), corpo.formatted(retroativo.getUuid(), ubs.getUuid(), "CONCLUIDO", hora), 201);
        assertThat(chegadaDe(retroativo)).isNull();
    }

    @Test
    void esperaComMediaMedianaEP90DescartandoInvalidos() throws Exception {
        atendimento(StatusAtendimento.CONCLUIDO, 180, 10L, 40L);
        atendimento(StatusAtendimento.CONCLUIDO, 170, 20L, 60L);
        atendimento(StatusAtendimento.CONCLUIDO, 160, null, 30L);
        // Consulta antes da chegada: erro de registro, descartado.
        atendimento(StatusAtendimento.CONCLUIDO, 150, null, -5L);
        LocalDate hoje = LocalDate.now();
        String r = ler("/api/v1/situacao/espera?de=" + hoje.minusDays(1) + "&ate=" + hoje + "&unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(r, "$.comChegada")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.descartados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.primeiroContato.amostra")).isEqualTo(3);
        assertThat(((Number) JsonPath.read(r, "$.primeiroContato.mediaMinutos")).doubleValue()).isEqualTo(20.0);
        assertThat(((Number) JsonPath.read(r, "$.primeiroContato.medianaMinutos")).doubleValue()).isEqualTo(20.0);
        assertThat(((Number) JsonPath.read(r, "$.primeiroContato.p90Minutos")).doubleValue()).isEqualTo(30.0);
        assertThat((Integer) JsonPath.read(r, "$.consulta.amostra")).isEqualTo(3);
        assertThat(((Number) JsonPath.read(r, "$.consulta.mediaMinutos")).doubleValue()).isEqualTo(43.3);
        assertThat(((Number) JsonPath.read(r, "$.consulta.medianaMinutos")).doubleValue()).isEqualTo(40.0);
        assertThat(((Number) JsonPath.read(r, "$.consulta.p90Minutos")).doubleValue()).isEqualTo(60.0);
        mockMvc.perform(get("/api/v1/situacao/espera?de=" + hoje + "&ate=" + hoje.minusDays(1))).andExpect(status().isBadRequest());
    }

    @Test
    void agoraComFilaEsperaEAlertas() throws Exception {
        atendimento(StatusAtendimento.EM_ANDAMENTO, 30, null, null);
        atendimento(StatusAtendimento.EM_ANDAMENTO, 200, 15L, null);
        atendimento(StatusAtendimento.EM_ANDAMENTO, 10, 2L, 5L);
        atendimento(StatusAtendimento.CONCLUIDO, 60, null, null);
        String r = ler("/api/v1/situacao?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(r, "$.emAtendimento")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(r, "$.aguardandoTriagem")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.aguardandoConsulta")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.maiorEsperaMinutos")).isBetween(199, 201);
        // Dois aguardando acima do limite 1 (fila, ALTA); 200 min acima de 120 (espera, MEDIA, abaixo do dobro).
        assertThat((List<String>) JsonPath.read(r, "$.alertas[?(@.tipo == 'FILA')].gravidade")).containsExactly("ALTA");
        assertThat((List<String>) JsonPath.read(r, "$.alertas[?(@.tipo == 'ESPERA')].gravidade")).containsExactly("MEDIA");
        assertThat((String) JsonPath.read(r, "$.alertas[0].gravidade")).isEqualTo("ALTA");
        assertThat(r).doesNotContain(PREFIXO + "Paciente");
    }

    @Test
    void ocupacaoIgualAAgenda() throws Exception {
        LocalDate hoje = LocalDate.now();
        String bloco = "{\"profissionalMatricula\": \"P-BI2-MED\", \"unidadeId\": \"%s\", \"diaSemana\": \"%s\", \"horaInicio\": \"08:00\", "
                + "\"horaFim\": \"10:00\", \"duracaoMinutos\": 30, \"tipo\": \"CONSULTA\", \"vigenteDesde\": \"%s\"}";
        enviar(post("/api/v1/agenda/bloco"), bloco.formatted(ubs.getUuid(), hoje.plusDays(2).getDayOfWeek(), hoje), 201);
        Paciente p = paciente();
        enviar(post("/api/v1/agendamento/"), ("{\"pacienteId\": \"%s\", \"profissionalMatricula\": \"P-BI2-MED\", \"unidadeId\": \"%s\", "
                + "\"dataHora\": \"%s\", \"status\": \"AGENDADO\", \"tipo\": \"CONSULTA\"}").formatted(p.getUuid(), ubs.getUuid(),
                hoje.plusDays(2).atTime(LocalTime.of(8, 30))), 201);

        String agenda = ler("/api/v1/agenda/?profissionalMatricula=P-BI2-MED&unidadeId=" + ubs.getUuid() + "&de=" + hoje + "&ate=" + hoje.plusDays(6));
        String r = ler("/api/v1/situacao/ocupacao?unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(r, "$.ofertadas")).isEqualTo((Integer) JsonPath.read(agenda, "$.vagasOfertadas")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.ocupadas")).isEqualTo((Integer) JsonPath.read(agenda, "$.vagasOcupadas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.percentual")).isEqualTo(25);
        assertThat((List<String>) JsonPath.read(r, "$.especialidades[*].especialidade")).containsExactly("Sem especialidade");
        mockMvc.perform(get("/api/v1/situacao/ocupacao?dias=40")).andExpect(status().isBadRequest());
    }

    @Test
    void escalaSemCoberturaEAfastadosSemExporLicencaMedica() throws Exception {
        Profissional ferias = profissional("P-BI2-FER", "90066600002", PREFIXO + "Em Férias");
        Profissional doente = profissional("P-BI2-LIC", "90066600003", PREFIXO + "De Licença");
        LocalDateTime daqui2h = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.HOURS);
        String turno = "insert into tb_turno_escala (uuid, unidade_id, profissional_id, funcao, tipo, inicio_em, fim_em, intervalo_minutos) "
                + "values (gen_random_uuid(), ?, ?, ?, 'MANHA', ?, ?, 0)";
        jdbc.update(turno, ubs.getUuid(), null, "MEDICO", Timestamp.valueOf(daqui2h), Timestamp.valueOf(daqui2h.plusHours(6)));
        jdbc.update(turno, ubs.getUuid(), ferias.getUuid(), "MEDICO", Timestamp.valueOf(daqui2h.plusDays(2)), Timestamp.valueOf(daqui2h.plusDays(2).plusHours(6)));
        jdbc.update(turno, ubs.getUuid(), doente.getUuid(), "MEDICO", Timestamp.valueOf(daqui2h.plusDays(3)), Timestamp.valueOf(daqui2h.plusDays(3).plusHours(6)));
        jdbc.update(turno, ubs.getUuid(), medico.getUuid(), "MEDICO", Timestamp.valueOf(daqui2h.plusDays(1)), Timestamp.valueOf(daqui2h.plusDays(1).plusHours(6)));
        String afastamento = "insert into tb_afastamento (uuid, profissional_id, tipo, status, data_inicio, data_fim) values (gen_random_uuid(), ?, ?, 'APROVADO', ?, ?)";
        LocalDate hoje = LocalDate.now();
        jdbc.update(afastamento, ferias.getUuid(), "FERIAS", hoje, hoje.plusDays(10));
        jdbc.update(afastamento, doente.getUuid(), "LICENCA_MEDICA", hoje, hoje.plusDays(10));

        String r = ler("/api/v1/situacao/escala?unidadeId=" + ubs.getUuid());
        assertThat((List<?>) JsonPath.read(r, "$")).hasSize(3);
        assertThat((List<String>) JsonPath.read(r, "$[?(@.situacao == 'SEM_COBERTURA')].funcao")).containsExactly("Médico");
        assertThat((List<String>) JsonPath.read(r, "$[?(@.profissionalNome == '" + PREFIXO + "Em Férias')].motivo")).containsExactly("Férias");
        assertThat((List<String>) JsonPath.read(r, "$[?(@.profissionalNome == '" + PREFIXO + "De Licença')].motivo")).containsExactly("Afastado");
        assertThat(r).doesNotContain("LICENCA_MEDICA").doesNotContain("Licença médica");

        // O turno descoberto das próximas 24 horas acende o alerta de escala.
        String agora = ler("/api/v1/situacao?unidadeId=" + ubs.getUuid());
        List<Map<String, Object>> escala = JsonPath.read(agora, "$.alertas[?(@.tipo == 'ESCALA')]");
        assertThat(escala).hasSize(1);
        assertThat(escala.get(0)).containsEntry("gravidade", "ALTA").containsEntry("rota", "/rh/escalas");
    }
}
