package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.BlocoAgendaRepository;
import com.edufelizardo.maissaudepublica.repositories.BloqueioAgendaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Agenda do profissional (ADR-0091): blocos, vagas, marcação na vaga ou como encaixe, bloqueio, afastamento do RH,
 * falta e resumo. Login e autorização desligados; as datas caem sempre na próxima segunda-feira.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendaControllerTest {

    private static final String AGENDA = "/api/v1/agenda/";
    private static final String AGENDAMENTO = "/api/v1/agendamento/";
    private static final String PREFIXO = "Agenda Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlocoAgendaRepository blocoRepository;

    @Autowired
    private BloqueioAgendaRepository bloqueioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outraUbs;
    private Profissional medica;
    private Paciente paciente;
    private LocalDate segunda;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outraUbs = unidade("Outra UBS");
        medica = new Profissional();
        medica.setMatricula("AGENDA-MED");
        medica.setCpf("91827364500");
        medica.setNome(PREFIXO + "Médica");
        medica.setAtivo(true);
        medica = profissionalRepository.save(medica);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        segunda = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    @AfterEach
    void limpar() {
        List<Profissional> profissionais = profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("AGENDA-")).toList();
        for (Profissional p : profissionais) {
            agendamentoRepository.deleteAll(agendamentoRepository.findAll().stream()
                    .filter(a -> a.getProfissional().getUuid().equals(p.getUuid())).toList());
            blocoRepository.deleteAll(blocoRepository.findByProfissional_UuidOrderByDiaSemanaAscHoraInicioAsc(p.getUuid()));
            afastamentoRepository.deleteAll(afastamentoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(p.getMatricula()));
        }
        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList();
        bloqueioRepository.deleteAll(bloqueioRepository.findAll().stream()
                .filter(b -> (b.getUnidade() != null && unidades.stream().anyMatch(u -> u.getUuid().equals(b.getUnidade().getUuid())))
                        || (b.getProfissional() != null && profissionais.stream().anyMatch(p -> p.getUuid().equals(b.getProfissional().getUuid()))))
                .toList());
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.deleteAll(profissionais);
        unidadeDeSaudeRepository.deleteAll(unidades);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private ResultActions bloco(UnidadeDeSaude unidade, String inicio, String fim, int duracao) throws Exception {
        return mockMvc.perform(post(AGENDA + "bloco").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "AGENDA-MED", "unidadeId": "%s", "diaSemana": "MONDAY", "horaInicio": "%s",
                 "horaFim": "%s", "duracaoMinutos": %d, "tipo": "CONSULTA"}
                """.formatted(unidade.getUuid(), inicio, fim, duracao)));
    }

    private ResultActions marcar(String hora, boolean encaixe) throws Exception {
        return mockMvc.perform(post(AGENDAMENTO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "profissionalMatricula": "AGENDA-MED", "dataHora": "%sT%s:00", "status": "AGENDADO",
                 "tipo": "CONSULTA", "unidadeId": "%s", "encaixe": %s}
                """.formatted(paciente.getUuid(), segunda, hora, ubs.getUuid(), encaixe)));
    }

    private String doDia() {
        return "?profissionalMatricula=AGENDA-MED&unidadeId=" + ubs.getUuid() + "&de=" + segunda + "&ate=" + segunda;
    }

    // ── Blocos ─────────────────────────────────────────────────────────────────────────────────

    @Test
    void blocoGeraVagasERecusaFimAntesDoInicioEBlocoCruzadoEmOutraUnidade() throws Exception {
        bloco(ubs, "08:00", "10:00", 30).andExpect(status().isCreated())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("Vagas por dia: 4")));
        bloco(ubs, "10:00", "09:00", 30).andExpect(status().isBadRequest());
        bloco(outraUbs, "09:30", "11:00", 30).andExpect(status().isConflict());
        bloco(outraUbs, "13:00", "15:00", 20).andExpect(status().isCreated());

        mockMvc.perform(get(AGENDA + "vagas" + doDia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].inicio").value(segunda + "T08:00:00"))
                .andExpect(jsonPath("$[3].fim").value(segunda + "T10:00:00"));
        mockMvc.perform(get(AGENDA + "bloco?profissionalMatricula=AGENDA-MED"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void encerrarOBlocoTiraAsVagasDepoisDaData() throws Exception {
        bloco(ubs, "08:00", "09:00", 30).andExpect(status().isCreated());
        UUID uuid = blocoRepository.findByProfissional_UuidOrderByDiaSemanaAscHoraInicioAsc(medica.getUuid()).get(0).getUuid();
        mockMvc.perform(post(AGENDA + "bloco/" + uuid + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"vigenteAte\": \"" + segunda.minusDays(1) + "\"}")).andExpect(status().isOk());
        mockMvc.perform(get(AGENDA + "vagas" + doDia())).andExpect(jsonPath("$.length()").value(0));
    }

    // ── Marcação ───────────────────────────────────────────────────────────────────────────────

    @Test
    void marcaNaVagaRecusaVagaOcupadaEForaDaAgendaEAceitaEncaixe() throws Exception {
        bloco(ubs, "08:00", "09:00", 30).andExpect(status().isCreated());

        marcar("08:00", false).andExpect(status().isCreated());
        marcar("08:00", false).andExpect(status().isUnprocessableEntity());
        marcar("08:10", false).andExpect(status().isUnprocessableEntity());
        marcar("08:10", true).andExpect(status().isCreated());

        mockMvc.perform(get(AGENDA + doDia()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vagasOfertadas").value(2))
                .andExpect(jsonPath("$.vagasOcupadas").value(1))
                .andExpect(jsonPath("$.ocupacaoPercentual").value(50))
                .andExpect(jsonPath("$.marcacoes").value(2))
                .andExpect(jsonPath("$.encaixes").value(1))
                .andExpect(jsonPath("$.dias[0].itens.length()").value(3))
                .andExpect(jsonPath("$.dias[0].itens[0].tipo").value("MARCACAO"))
                .andExpect(jsonPath("$.dias[0].itens[0].pacienteNome").value(PREFIXO + "Paciente"))
                .andExpect(jsonPath("$.dias[0].itens[1].tipo").value("ENCAIXE"))
                .andExpect(jsonPath("$.dias[0].itens[2].tipo").value("VAGA"));
        mockMvc.perform(get(AGENDA + "vagas" + doDia())).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void faltaEntraNoResumoESoValeUmaVez() throws Exception {
        bloco(ubs, "08:00", "09:00", 30).andExpect(status().isCreated());
        marcar("08:30", false).andExpect(status().isCreated());
        UUID agendamento = agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(paciente.getUuid()).get(0).getUuid();

        mockMvc.perform(post(AGENDAMENTO + agendamento + "/falta")).andExpect(status().isOk());
        mockMvc.perform(post(AGENDAMENTO + agendamento + "/falta")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(AGENDAMENTO + agendamento)).andExpect(jsonPath("$.status").value("FALTOU"));
        mockMvc.perform(get(AGENDA + doDia())).andExpect(jsonPath("$.faltas").value(1));
    }

    // ── Bloqueio e afastamento ─────────────────────────────────────────────────────────────────

    @Test
    void bloqueioTiraAVagaERecusaAteEncaixeERemoverDevolve() throws Exception {
        bloco(ubs, "08:00", "09:00", 30).andExpect(status().isCreated());
        mockMvc.perform(post(AGENDA + "bloqueio").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "inicio": "%sT08:00:00", "fim": "%sT08:30:00", "motivo": "REUNIAO", "descricao": "Reunião de equipe"}
                """.formatted(ubs.getUuid(), segunda, segunda))).andExpect(status().isCreated());

        mockMvc.perform(get(AGENDA + "vagas" + doDia()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].inicio").value(segunda + "T08:30:00"));
        marcar("08:00", true).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(AGENDA + doDia()))
                .andExpect(jsonPath("$.vagasOfertadas").value(1))
                .andExpect(jsonPath("$.dias[0].itens[0].tipo").value("BLOQUEIO"));

        UUID bloqueio = bloqueioRepository.findAll().stream()
                .filter(b -> b.getUnidade() != null && b.getUnidade().getUuid().equals(ubs.getUuid())).findFirst().orElseThrow().getUuid();
        mockMvc.perform(delete(AGENDA + "bloqueio/" + bloqueio)).andExpect(status().isOk());
        mockMvc.perform(get(AGENDA + "vagas" + doDia())).andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(post(AGENDA + "bloqueio").contentType(MediaType.APPLICATION_JSON).content("""
                {"inicio": "%sT08:00:00", "fim": "%sT08:30:00", "motivo": "OUTRO"}
                """.formatted(segunda, segunda))).andExpect(status().isBadRequest());
    }

    @Test
    void feriasAprovadasNoRhFecharamAAgenda() throws Exception {
        bloco(ubs, "08:00", "09:00", 30).andExpect(status().isCreated());
        Afastamento ferias = new Afastamento();
        ferias.setProfissional(medica);
        ferias.setTipo(TipoAfastamento.FERIAS);
        ferias.setStatus(StatusAfastamento.APROVADO);
        ferias.setDataInicio(segunda.minusDays(3));
        ferias.setDataFim(segunda.plusDays(10));
        afastamentoRepository.save(ferias);

        mockMvc.perform(get(AGENDA + "vagas" + doDia())).andExpect(jsonPath("$.length()").value(0));
        marcar("08:00", true).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(AGENDA + doDia()))
                .andExpect(jsonPath("$.vagasOfertadas").value(0))
                .andExpect(jsonPath("$.dias[0].itens[0].tipo").value("AFASTAMENTO"))
                .andExpect(jsonPath("$.dias[0].itens[0].descricao").value("FERIAS"));
    }

    @Test
    void marcacaoSemUnidadeContinuaComoAntesDaAgenda() throws Exception {
        mockMvc.perform(post(AGENDAMENTO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "profissionalMatricula": "AGENDA-MED", "dataHora": "%sT07:13:00", "status": "AGENDADO", "tipo": "RETORNO"}
                """.formatted(paciente.getUuid(), segunda))).andExpect(status().isCreated());
        mockMvc.perform(get(AGENDA + "?profissionalMatricula=AGENDA-MED&unidadeId=" + ubs.getUuid() + "&de=" + segunda + "&ate=" + segunda.plusDays(70)))
                .andExpect(status().isBadRequest());
    }
}
