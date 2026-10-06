package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.CategoriaSalarial;
import com.edufelizardo.maissaudepublica.models.HorarioUnidade;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.CategoriaSalarialRepository;
import com.edufelizardo.maissaudepublica.repositories.HorarioUnidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escalas (ADR-0105): turno no horário da unidade, plantão só em unidade 24 horas, lotação e afastamento do RH, sobreposição,
 * vaga e designação, alertas de jornada e descanso, edição, remoção e cópia de semana.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EscalaControllerTest {

    private static final String URL = "/api/v1/escala/";
    private static final String PREFIXO = "Escala Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private CategoriaSalarialRepository categoriaSalarialRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private HorarioUnidadeRepository horarioRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude upa;
    /** Segunda-feira da semana que vem: tudo no futuro. */
    private LocalDate seg;

    @BeforeEach
    void seed() {
        limpar();
        seg = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        ubs = unidade("UBS Vila Esperança", TipoUnidadeDeSaude.UBS, false);
        upa = unidade("UPA São Pedro", TipoUnidadeDeSaude.UPA, true);
        for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
            horarioRepository.save(new HorarioUnidade(null, ubs, d, LocalTime.of(7, 0), LocalTime.of(12, 0)));
            horarioRepository.save(new HorarioUnidade(null, ubs, d, LocalTime.of(13, 0), LocalTime.of(19, 0)));
        }
        CategoriaSalarial categoria = new CategoriaSalarial();
        categoria.setNome(PREFIXO + "Categoria");
        categoria = categoriaSalarialRepository.save(categoria);
        Cargo cargo = cargoRepository.save(new Cargo(categoria, PREFIXO + "Cargo"));
        lotacaoRepository.save(new Lotacao(profissional("MED"), ubs, cargo, 20, LocalDate.now().minusYears(1), "admissão"));
        lotacaoRepository.save(new Lotacao(profissional("ENF"), ubs, cargo, 40, LocalDate.now().minusYears(1), "admissão"));
        lotacaoRepository.save(new Lotacao(profissional("PLA"), upa, cargo, 36, LocalDate.now().minusYears(1), "admissão"));
        profissional("SEM");
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_turno_escala where unidade_id = ?", u);
            jdbc.update("delete from tb_horario_unidade where unidade_id = ?", u);
            jdbc.update("delete from tb_lotacao where unidade_id = ?", u);
        }
        jdbc.update("delete from tb_afastamento where profissional_id in (select uuid from tb_profissional where matricula like 'ES-%')");
        unidadeDeSaudeRepository.deleteAllById(unidades);
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("ES-")).toList());
        jdbc.update("delete from tb_cargo where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_categoria_salarial where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, boolean h24) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setFunciona24h(h24);
        return unidadeDeSaudeRepository.save(u);
    }

    private Profissional profissional(String sufixo) {
        Profissional p = new Profissional();
        p.setMatricula("ES-" + sufixo);
        p.setCpf(String.valueOf(91000000000L + Math.abs(sufixo.hashCode() % 999999)));
        p.setNome(PREFIXO + sufixo);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private ResultActions turno(UnidadeDeSaude u, String matricula, String funcao, String tipo, LocalDate data, String inicio, String fim)
            throws Exception {
        return mockMvc.perform(post(URL + "turno").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "profissionalMatricula": %s, "funcao": %s, "tipo": "%s", "data": "%s", "inicio": "%s", "fim": "%s"}
                """.formatted(u.getUuid(), matricula == null ? "null" : "\"ES-" + matricula + "\"", funcao == null ? "null" : "\"" + funcao + "\"",
                tipo, data, inicio, fim)));
    }

    private ResultActions turnoComIntervalo(UnidadeDeSaude u, String matricula, String tipo, LocalDate data, String inicio, String fim,
                                            int intervalo) throws Exception {
        return mockMvc.perform(post(URL + "turno").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "profissionalMatricula": "ES-%s", "tipo": "%s", "data": "%s", "inicio": "%s", "fim": "%s", "intervaloMinutos": %d}
                """.formatted(u.getUuid(), matricula, tipo, data, inicio, fim, intervalo)));
    }

    private ResultActions modelo(UnidadeDeSaude u, String matricula, String modelo) throws Exception {
        return mockMvc.perform(post(URL + "aplicar-modelo").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "profissionalMatricula": "ES-%s", "modelo": "%s", "semana": "%s"}
                """.formatted(u.getUuid(), matricula, modelo, seg.plusDays(2))));
    }

    private String criado(ResultActions r) throws Exception {
        return JsonPath.read(r.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.uuid");
    }

    @Test
    void turnoNoHorarioEntraNaSemana() throws Exception {
        turno(ubs, "MED", null, "MANHA", seg, "07:00", "13:00").andExpect(status().isCreated())
                .andExpect(jsonPath("$.horas").value(6.0)).andExpect(jsonPath("$.vaga").value(false));
        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.plusDays(3).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicio").value(seg.toString()))
                .andExpect(jsonPath("$.turnos").value(1))
                .andExpect(jsonPath("$.horasPrevistas").value(6.0))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-MED')].horas").value(hasItem(6.0)))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].turnos[*]").isEmpty());
    }

    @Test
    void foraDoHorarioOuDiaFechadoRecusa() throws Exception {
        turno(ubs, "MED", null, "MANHA", seg, "06:00", "12:00").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("07:00 às 19:00")));
        turno(ubs, "MED", null, "MANHA", seg.plusDays(5), "08:00", "12:00").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("não abre")));
        turno(ubs, "MED", null, "MANHA", seg, "10:00", "09:00").andExpect(status().isBadRequest());
    }

    @Test
    void plantaoSoEmUnidade24Horas() throws Exception {
        turno(ubs, "ENF", null, "PLANTAO_12H", seg, "07:00", "19:00").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("24 horas")));
        turno(upa, "PLA", null, "PLANTAO_12H", seg, "19:00", "07:00").andExpect(status().isCreated())
                .andExpect(jsonPath("$.fimEm").value(seg.plusDays(1) + "T07:00:00"));
        turno(upa, "PLA", null, "PLANTAO_12H", seg.plusDays(2), "07:00", "18:00").andExpect(status().isBadRequest());
        turno(upa, "PLA", null, "PLANTAO_24H", seg.plusDays(4), "07:00", "07:00").andExpect(status().isCreated())
                .andExpect(jsonPath("$.horas").value(24.0));
    }

    @Test
    void lotacaoAfastamentoESobreposicao() throws Exception {
        turno(ubs, "SEM", null, "MANHA", seg, "07:00", "11:00").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("lotação")));
        turno(ubs, "PLA", null, "MANHA", seg, "07:00", "11:00").andExpect(status().isUnprocessableEntity());

        afastamentoRepository.save(new Afastamento(profissionalRepository.findByMatricula("ES-ENF").orElseThrow(), TipoAfastamento.LICENCA_MEDICA,
                seg.plusDays(1), seg.plusDays(3), StatusAfastamento.APROVADO, "CID reservado"));
        turno(ubs, "ENF", null, "TARDE", seg.plusDays(2), "13:00", "19:00").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("de licença")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(containsString("médica"))));

        turno(ubs, "MED", null, "MANHA", seg, "07:00", "13:00").andExpect(status().isCreated());
        turno(ubs, "MED", null, "TARDE", seg, "12:00", "16:00").andExpect(status().isConflict());
        turno(ubs, "MED", null, "TARDE", seg, "13:00", "17:00").andExpect(status().isCreated());

        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(jsonPath("$.afastados").value(1))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].ausencias[0].tipo").value(hasItem("LICENCA_MEDICA")))
                .andExpect(jsonPath("$.ausencias[?(@.matricula == 'ES-ENF')]").isNotEmpty());
    }

    @Test
    void vagaDesignacaoETroca() throws Exception {
        turno(ubs, null, null, "MANHA", seg, "07:00", "13:00").andExpect(status().isBadRequest());
        String vaga = criado(turno(ubs, null, "MEDICO", "MANHA", seg, "07:00", "13:00"));
        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(jsonPath("$.vagasAbertas").value(1)).andExpect(jsonPath("$.vagas", hasSize(1)));

        mockMvc.perform(post(URL + "turno/" + vaga + "/designar").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "ES-SEM"}""")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(URL + "turno/" + vaga + "/designar").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "ES-MED"}""")).andExpect(status().isOk())
                .andExpect(jsonPath("$.vaga").value(false)).andExpect(jsonPath("$.profissionalMatricula").value("ES-MED"));
        mockMvc.perform(post(URL + "turno/" + vaga + "/designar").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "ES-MED"}""")).andExpect(status().isConflict());
        mockMvc.perform(post(URL + "turno/" + vaga + "/designar").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "ES-ENF", "motivo": "Troca combinada"}""")).andExpect(status().isOk())
                .andExpect(jsonPath("$.profissionalMatricula").value("ES-ENF"));
        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(jsonPath("$.vagasAbertas").value(0));
    }

    @Test
    void alertasDeJornadaEDescanso() throws Exception {
        for (int d = 0; d < 3; d++) {
            turnoComIntervalo(ubs, "MED", "MANHA", seg.plusDays(d), "07:00", "13:15", 15).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.alertas").isEmpty());
        }
        turnoComIntervalo(ubs, "MED", "TARDE", seg.plusDays(3), "13:00", "19:00", 15).andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertas[0]").value(containsString("acima da jornada contratada de 20h")));

        turno(upa, "PLA", null, "PLANTAO_12H", seg, "19:00", "07:00").andExpect(status().isCreated());
        turnoComIntervalo(upa, "PLA", "TARDE", seg.plusDays(1), "13:00", "19:00", 15).andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertas[0]").value(containsString("Descanso de 6h")));

        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(jsonPath("$.comAlerta").value(1))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-MED')].horas").value(hasItem(23.8)));
    }

    @Test
    void edicaoRemocaoEDiaPassado() throws Exception {
        turno(ubs, "MED", null, "MANHA", LocalDate.now().minusDays(1), "07:00", "11:00").andExpect(status().isUnprocessableEntity());
        String id = criado(turno(ubs, "MED", null, "MANHA", seg, "07:00", "11:00"));
        mockMvc.perform(patch(URL + "turno/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "profissionalMatricula": "ES-ENF", "tipo": "MANHA", "data": "%s", "inicio": "07:00", "fim": "11:00"}
                """.formatted(ubs.getUuid(), seg))).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(URL + "turno/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "profissionalMatricula": "ES-MED", "tipo": "TARDE", "data": "%s", "inicio": "14:00", "fim": "18:00", "descricao": "Pré-natal"}
                """.formatted(ubs.getUuid(), seg.plusDays(1)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TARDE")).andExpect(jsonPath("$.descricao").value("Pré-natal"));
        mockMvc.perform(delete(URL + "turno/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(delete(URL + "turno/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void copiaDeSemanaDeixaDeForaOQueQuebraRegra() throws Exception {
        turno(ubs, "MED", null, "MANHA", seg, "07:00", "13:00").andExpect(status().isCreated());
        turno(ubs, "ENF", null, "TARDE", seg.plusDays(1), "13:00", "19:00").andExpect(status().isCreated());
        turno(ubs, null, "ACS", "MANHA", seg.plusDays(2), "07:00", "11:00").andExpect(status().isCreated());
        afastamentoRepository.save(new Afastamento(profissionalRepository.findByMatricula("ES-ENF").orElseThrow(), TipoAfastamento.FERIAS,
                seg.plusDays(7), seg.plusDays(20), StatusAfastamento.APROVADO, null));

        mockMvc.perform(post(URL + "copiar-semana").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "origem": "%s", "destino": "%s"}""".formatted(ubs.getUuid(), seg.plusDays(2), seg.plusDays(9))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.copiados").value(2))
                .andExpect(jsonPath("$.ignorados", hasSize(1)))
                .andExpect(jsonPath("$.ignorados[0].motivo").value(containsString("em férias")));
        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.plusDays(7).toString()))
                .andExpect(jsonPath("$.turnos").value(2)).andExpect(jsonPath("$.vagasAbertas").value(1));
        mockMvc.perform(post(URL + "copiar-semana").contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "origem": "%s", "destino": "%s"}""".formatted(ubs.getUuid(), seg, seg.plusDays(1))))
                .andExpect(status().isBadRequest());
    }
    @Test
    void intervaloNaoContaNaJornada() throws Exception {
        for (int d = 0; d < 5; d++) {
            turnoComIntervalo(ubs, "ENF", "DIURNO", seg.plusDays(d), "08:00", "17:00", 60).andExpect(status().isCreated())
                    .andExpect(jsonPath("$.horas").value(8.0)).andExpect(jsonPath("$.intervaloMinutos").value(60))
                    .andExpect(jsonPath("$.alertas").isEmpty());
        }
        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].horas").value(hasItem(40.0)))
                .andExpect(jsonPath("$.horasPrevistas").value(40.0))
                .andExpect(jsonPath("$.comAlerta").value(0));

        turnoComIntervalo(ubs, "MED", "DIURNO", seg, "08:00", "17:00", 0).andExpect(status().isCreated())
                .andExpect(jsonPath("$.horas").value(9.0))
                .andExpect(jsonPath("$.alertas[0]").value(containsString("Intervalo de 0min")));
        turnoComIntervalo(ubs, "MED", "TARDE", seg.plusDays(1), "13:00", "17:00", 150).andExpect(status().isBadRequest());
        turnoComIntervalo(ubs, "MED", "MANHA", seg.plusDays(1), "07:00", "08:00", 60).andExpect(status().isBadRequest());
        turnoComIntervalo(upa, "PLA", "PLANTAO_12H", seg, "07:00", "19:00", 0).andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertas").isEmpty());
    }

    @Test
    void modelo40hGeraCincoDiasDe8hMais1h() throws Exception {
        modelo(ubs, "ENF", "H40_8H").andExpect(status().isOk())
                .andExpect(jsonPath("$.criados").value(5))
                .andExpect(jsonPath("$.horas").value(40.0))
                .andExpect(jsonPath("$.ignorados").isEmpty());
        mockMvc.perform(get(URL).param("unidadeId", ubs.getUuid().toString()).param("semana", seg.toString()))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].horas").value(hasItem(40.0)))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].alertas[*]").isEmpty())
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].turnos[0].tipo").value(hasItem("DIURNO")))
                .andExpect(jsonPath("$.linhas[?(@.matricula == 'ES-ENF')].turnos[0].fimEm").value(hasItem(seg + "T17:00:00")));
    }

    @Test
    void modeloDeixaDeForaOQueQuebraRegra() throws Exception {
        afastamentoRepository.save(new Afastamento(profissionalRepository.findByMatricula("ES-ENF").orElseThrow(), TipoAfastamento.FERIAS,
                seg.plusDays(2), seg.plusDays(2), StatusAfastamento.APROVADO, null));
        modelo(ubs, "ENF", "H40_8H").andExpect(jsonPath("$.criados").value(4))
                .andExpect(jsonPath("$.ignorados[0].motivo").value(containsString("em férias")));
        modelo(ubs, "MED", "H12X36").andExpect(jsonPath("$.criados").value(0)).andExpect(jsonPath("$.ignorados", hasSize(4)))
                .andExpect(jsonPath("$.ignorados[0].motivo").value(containsString("24 horas")));
        modelo(upa, "PLA", "H12X36").andExpect(jsonPath("$.criados").value(4)).andExpect(jsonPath("$.horas").value(48.0));
        modelo(ubs, "MED", "H44_6X1").andExpect(jsonPath("$.criados").value(5))
                .andExpect(jsonPath("$.ignorados[0].motivo").value(containsString("não abre")));
    }
}
