package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Produção assistencial por unidade, fatia BI1 (ADR-0143): contagens, distribuições, período anterior, agenda, série,
 * ranking e o CSV, sobre um conjunto controlado numa unidade nova. Só números.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProducaoAssistencialControllerTest {

    private static final String PREFIXO = "Producao Teste ";
    private static final String URL = "/api/v1/producao";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        Profissional medico = profissional("P-BI1-MED", "90055500001", "CRM-SP");
        Profissional enfermeiro = profissional("P-BI1-ENF", "90055500002", "COREN");
        Paciente neonato = paciente("Neonato", "90055500011", Sexo.MASCULINO, hoje.minusDays(20));
        Paciente crianca = paciente("Criança", "90055500012", Sexo.FEMININO, hoje.minusYears(8));
        Paciente adulta = paciente("Adulta", "90055500013", Sexo.FEMININO, hoje.minusYears(30));
        Paciente idoso = paciente("Idoso", "90055500014", Sexo.MASCULINO, hoje.minusYears(70));

        Atendimento a = atendimento(neonato, medico, ubs, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO, 5, 9);
        String especialidade = jdbc.queryForObject("select cast(uuid as varchar) from tb_especialidade where not odontologica order by nome limit 1",
                String.class);
        jdbc.update("update tb_atendimento set especialidade_id = cast(? as uuid) where uuid = ?", especialidade, a.getUuid());
        atendimento(crianca, enfermeiro, ubs, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO, 4, 9);
        atendimento(adulta, medico, ubs, TipoAtendimento.URGENCIA, StatusAtendimento.CONCLUIDO, 3, 14);
        atendimento(idoso, enfermeiro, ubs, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO, 2, 14);
        // Em andamento não conta; nem a outra unidade quando se pede a UBS.
        atendimento(adulta, medico, ubs, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, 1, 10);
        atendimento(adulta, medico, outra, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO, 1, 10);
        // No período anterior (os 10 dias antes).
        atendimento(adulta, medico, ubs, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO, 15, 10);

        agendamento(adulta, medico, StatusAgendamento.REALIZADO, false, 3);
        agendamento(idoso, medico, StatusAgendamento.FALTOU, false, 2);
        agendamento(crianca, medico, StatusAgendamento.CANCELADO, true, 2);
        agendamento(neonato, medico, StatusAgendamento.AGENDADO, true, 0);
        // A marcação sem unidade (anterior à agenda por unidade) não entra em nenhuma abrangência, nem derruba a série.
        agendamentoRepository.save(new Agendamento(adulta, medico, LocalDateTime.of(hoje.minusDays(40), LocalTime.of(8, 0)),
                StatusAgendamento.FALTOU, TipoAgendamento.CONSULTA, null));
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_atendimento where unidade_id in " + unidades
                + " or paciente_id in (select uuid from tb_paciente where nome like '" + PREFIXO + "%')");
        jdbc.update("delete from tb_agendamento where unidade_id in " + unidades
                + " or paciente_id in (select uuid from tb_paciente where nome like '" + PREFIXO + "%')");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'P-BI1-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String matricula, String cpf, String conselho) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setConselhoClasse(conselho);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private Paciente paciente(String nome, String cpf, Sexo sexo, LocalDate nascimento) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setSexo(sexo);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private Atendimento atendimento(Paciente p, Profissional pr, UnidadeDeSaude u, TipoAtendimento tipo, StatusAtendimento status,
                                    int diasAtras, int hora) {
        return atendimentoRepository.save(new Atendimento(p, pr, u, null, null, tipo, status,
                LocalDateTime.of(hoje.minusDays(diasAtras), LocalTime.of(hora, 15))));
    }

    private void agendamento(Paciente p, Profissional pr, StatusAgendamento status, boolean encaixe, int diasAtras) {
        Agendamento g = new Agendamento(p, pr, LocalDateTime.of(hoje.minusDays(diasAtras), LocalTime.of(8, 0)), status, TipoAgendamento.CONSULTA, null);
        g.setUnidade(ubs);
        g.setEncaixe(encaixe);
        agendamentoRepository.save(g);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static long contagem(String json, String lista, String chave) {
        List<Object> v = JsonPath.read(json, "$." + lista + "[?(@.chave == '" + chave + "')].quantidade");
        return v.isEmpty() ? -1 : ((Number) v.get(0)).longValue();
    }

    @Test
    void producaoDaUnidadeComDistribuicoesEAgenda() throws Exception {
        String r = ler(URL + "?de=" + hoje.minusDays(9) + "&ate=" + hoje + "&unidadeId=" + ubs.getUuid());
        assertThat((Integer) JsonPath.read(r, "$.atendimentos")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.atendimentosAnterior")).isEqualTo(1);
        assertThat(((Number) JsonPath.read(r, "$.variacao")).doubleValue()).isEqualTo(300.0);
        assertThat(((Number) JsonPath.read(r, "$.mediaDiaria")).doubleValue()).isEqualTo(0.4);
        assertThat((String) JsonPath.read(r, "$.abrangencia")).isEqualTo(PREFIXO + "UBS");
        assertThat((String) JsonPath.read(r, "$.anteriorDe")).isEqualTo(hoje.minusDays(19).toString());

        assertThat(contagem(r, "porTipo", "CONSULTA")).isEqualTo(3);
        assertThat(contagem(r, "porTipo", "URGENCIA")).isEqualTo(1);
        assertThat(contagem(r, "porTipo", "INTERNACAO")).isZero();
        assertThat(contagem(r, "porCategoria", "MED")).isEqualTo(2);
        assertThat(contagem(r, "porCategoria", "ENF")).isEqualTo(2);
        assertThat(contagem(r, "porEspecialidade", "Sem especialidade")).isEqualTo(3);
        for (String faixa : List.of("NEONATAL", "CRIANCA", "ADULTO", "IDOSO")) {
            assertThat(contagem(r, "porFaixaEtaria", faixa)).as(faixa).isEqualTo(1);
        }
        assertThat(contagem(r, "porFaixaEtaria", "ADOLESCENTE")).isZero();
        assertThat(contagem(r, "porSexo", "FEMININO")).isEqualTo(2);
        assertThat(contagem(r, "porSexo", "MASCULINO")).isEqualTo(2);
        List<Integer> horas = JsonPath.read(r, "$.porHora");
        assertThat(horas).hasSize(24);
        assertThat(horas.get(9)).isEqualTo(2);
        assertThat(horas.get(14)).isEqualTo(2);

        Map<String, Object> agenda = JsonPath.read(r, "$.agenda");
        assertThat(agenda).containsEntry("marcados", 4).containsEntry("realizados", 1).containsEntry("faltas", 1)
                .containsEntry("cancelados", 1).containsEntry("encaixes", 1);
        assertThat(((Number) agenda.get("absenteismo")).doubleValue()).isEqualTo(50.0);
        assertThat((Integer) JsonPath.read(r, "$.agendaAnterior.marcados")).isZero();
        assertThat((Object) JsonPath.read(r, "$.agendaAnterior.absenteismo")).isNull();
        assertThat(r).doesNotContain(PREFIXO + "Adulta");
    }

    @Test
    void serieRankingECsv() throws Exception {
        String dia = ler(URL + "/serie?granularidade=DIA&pontos=10&unidadeId=" + ubs.getUuid());
        List<Integer> atend = JsonPath.read(dia, "$.pontos[*].atendimentos");
        assertThat(atend).hasSize(10);
        assertThat(atend.stream().mapToInt(Integer::intValue).sum()).isEqualTo(4);
        assertThat((String) JsonPath.read(dia, "$.pontos[9].periodo")).isEqualTo(hoje.toString());
        assertThat(((List<Integer>) JsonPath.read(dia, "$.pontos[*].faltas")).stream().mapToInt(Integer::intValue).sum()).isEqualTo(1);

        String mes = ler(URL + "/serie?unidadeId=" + ubs.getUuid());
        List<Integer> meses = JsonPath.read(mes, "$.pontos[*].atendimentos");
        assertThat(meses).hasSize(12);
        assertThat(meses.stream().mapToInt(Integer::intValue).sum()).isEqualTo(5);

        String ranking = ler(URL + "/unidades?de=" + hoje.minusDays(9) + "&ate=" + hoje);
        assertThat((List<Integer>) JsonPath.read(ranking, "$[?(@.unidadeId == '" + ubs.getUuid() + "')].atendimentos")).containsExactly(4);
        assertThat((List<Integer>) JsonPath.read(ranking, "$[?(@.unidadeId == '" + ubs.getUuid() + "')].atendimentosAnterior")).containsExactly(1);
        assertThat((List<Integer>) JsonPath.read(ranking, "$[?(@.unidadeId == '" + outra.getUuid() + "')].atendimentos")).containsExactly(1);

        String csv = ler(URL + "/exportacao?de=" + hoje.minusDays(9) + "&ate=" + hoje + "&unidadeId=" + ubs.getUuid());
        assertThat(csv).contains("Atendimentos concluídos;4;1").contains("Absenteísmo (%);50.0;").contains("Urgência;1")
                .doesNotContain(PREFIXO + "Adulta");
    }

    @Test
    void periodoEParametrosInvalidos() throws Exception {
        mockMvc.perform(get(URL + "?de=" + hoje + "&ate=" + hoje.minusDays(1))).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "?de=" + hoje.minusDays(400) + "&ate=" + hoje)).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "/serie?granularidade=DIA&pontos=91")).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "/serie?granularidade=MES&pontos=0")).andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "?unidadeId=" + UUID.randomUUID())).andExpect(status().isNotFound());
    }
}
