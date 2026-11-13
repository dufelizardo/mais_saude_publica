package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Internação eletiva agendada e censo hospitalar diário (ADR-0185), com login e autorização desligados. O escopo fica em
 * {@link EletivaCensoAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EletivaCensoControllerTest {

    private static final String ELETIVA = "/api/v1/internacao-eletiva/";
    private static final String CENSO = "/api/v1/censo-diario/";
    private static final String PREFIXO = "EletCenso Teste ";

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
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude hospital;
    private Setor cirurgia;

    @BeforeEach
    void seed() {
        limpar();
        hospital = new UnidadeDeSaude();
        hospital.setNome(PREFIXO + "Hospital");
        hospital.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        hospital.setAtivo(true);
        hospital = unidadeDeSaudeRepository.save(hospital);
        cirurgia = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica cirúrgica", "EC-CC", TipoSetor.ASSISTENCIAL, true, null));
        Profissional medico = new Profissional();
        medico.setMatricula("EC-MED");
        medico.setCpf("19283746546");
        medico.setNome(PREFIXO + "Cirurgiã");
        medico.setAtivo(true);
        profissionalRepository.save(medico);
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_censo_diario_paciente where censo_id in (select uuid from tb_censo_diario where unidade_id = ?)", u);
            jdbc.update("delete from tb_censo_diario where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_internacao_agendada where agendamento_id in (select uuid from tb_internacao_agendada where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao_agendada where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.findByMatricula("EC-MED").ifPresent(profissionalRepository::delete);
    }

    // ── Eletiva ────────────────────────────────────────────────────────────────────────────────

    @Test
    void agendaReservaNaJanelaEAdmiteComoEletiva() throws Exception {
        Paciente ana = paciente("Ana");
        UUID cirurgico = leito("CC-01", TipoLeito.CIRURGICO);
        UUID clinico = leito("CC-02", TipoLeito.CLINICO);
        UUID agendamento = agendar(ana, LocalDate.now().plusDays(1));
        mockMvc.perform(post(ELETIVA).contentType(MediaType.APPLICATION_JSON).content(corpoAgendamento(ana, LocalDate.now().plusDays(3))))
                .andExpect(status().isConflict());
        mockMvc.perform(post(ELETIVA).contentType(MediaType.APPLICATION_JSON).content(corpoAgendamento(paciente("Bia"), LocalDate.now().minusDays(1))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(ELETIVA + "?unidadeId=" + hospital.getUuid()))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].podeReservar".formatted(agendamento)).value(org.hamcrest.Matchers.contains(true)));

        acao(agendamento, "reserva", "{\"profissionalMatricula\": \"EC-MED\", \"leitoId\": \"%s\"}".formatted(clinico)).andExpect(status().isUnprocessableEntity());
        acao(agendamento, "reserva", "{\"profissionalMatricula\": \"EC-MED\", \"leitoId\": \"%s\"}".formatted(cirurgico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVADA"))
                .andExpect(jsonPath("$.leitoReservadoIdentificacao").value("CC-01"));
        Leito reservado = leitoRepository.findById(cirurgico).orElseThrow();
        assertThat(reservado.getSituacao()).isEqualTo(SituacaoLeito.RESERVADO);
        assertThat(reservado.getReservaAgendamentoId()).isEqualTo(agendamento);
        mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "EC-MED", "cid": "K80.2", "motivo": "Outro.", "carater": "URGENCIA"}
                """.formatted(paciente("Caio").getUuid(), cirurgico))).andExpect(status().isUnprocessableEntity());

        acao(agendamento, "admissao", "{\"profissionalMatricula\": \"EC-MED\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADMITIDA"))
                .andExpect(jsonPath("$.eventos[*].tipo").value(org.hamcrest.Matchers.contains("AGENDAMENTO", "RESERVA", "ADMISSAO")));
        assertThat(jdbc.queryForObject("select carater from tb_internacao where leito_id = ?", String.class, cirurgico)).isEqualTo("ELETIVA");
        assertThat(leitoRepository.findById(cirurgico).orElseThrow().getSituacao()).isEqualTo(SituacaoLeito.OCUPADO);
    }

    @Test
    void reservaSoNaJanelaDeDoisDiasEAdmissaoSemReservaPedeLeito() throws Exception {
        UUID cirurgico = leito("CC-10", TipoLeito.CIRURGICO);
        UUID longe = agendar(paciente("Davi"), LocalDate.now().plusDays(5));
        acao(longe, "reserva", "{\"profissionalMatricula\": \"EC-MED\", \"leitoId\": \"%s\"}".formatted(cirurgico)).andExpect(status().isUnprocessableEntity());
        UUID hoje = agendar(paciente("Eva"), LocalDate.now());
        acao(hoje, "admissao", "{\"profissionalMatricula\": \"EC-MED\"}").andExpect(status().isBadRequest());
        acao(hoje, "admissao", "{\"profissionalMatricula\": \"EC-MED\", \"leitoId\": \"%s\"}".formatted(cirurgico))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ADMITIDA"));
    }

    @Test
    void faltaRemarcacaoECancelamentoDesfazemAReserva() throws Exception {
        UUID cirurgico = leito("CC-20", TipoLeito.CIRURGICO);
        UUID amanha = agendar(paciente("Fabi"), LocalDate.now().plusDays(1));
        acao(amanha, "reserva", "{\"profissionalMatricula\": \"EC-MED\", \"leitoId\": \"%s\"}".formatted(cirurgico)).andExpect(status().isOk());
        acao(amanha, "falta", "{\"profissionalMatricula\": \"EC-MED\", \"motivo\": \"Não veio.\"}").andExpect(status().isUnprocessableEntity());
        acao(amanha, "remarcacao", "{\"profissionalMatricula\": \"EC-MED\", \"dataPrevista\": \"%s\"}".formatted(LocalDate.now().plusDays(4)))
                .andExpect(status().isBadRequest());
        acao(amanha, "remarcacao", "{\"profissionalMatricula\": \"EC-MED\", \"dataPrevista\": \"%s\", \"motivo\": \"Sem hemoderivados.\"}"
                .formatted(LocalDate.now().plusDays(4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.dataPrevista").value(LocalDate.now().plusDays(4).toString()));
        assertThat(leitoRepository.findById(cirurgico).orElseThrow().getSituacao()).isEqualTo(SituacaoLeito.LIVRE);
        acao(amanha, "cancelamento", "{\"profissionalMatricula\": \"EC-MED\", \"motivo\": \"Desistiu da cirurgia.\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));

        UUID hoje = agendar(paciente("Gil"), LocalDate.now());
        acao(hoje, "falta", "{\"profissionalMatricula\": \"EC-MED\", \"motivo\": \"Não compareceu.\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FALTA"));
    }

    @Test
    void reservaVencidaVoltaOLeitoEOAgendamentoAguardaDesfecho() throws Exception {
        UUID cirurgico = leito("CC-30", TipoLeito.CIRURGICO);
        UUID agendamento = agendar(paciente("Hugo"), LocalDate.now());
        acao(agendamento, "reserva", "{\"profissionalMatricula\": \"EC-MED\", \"leitoId\": \"%s\"}".formatted(cirurgico)).andExpect(status().isOk());
        jdbc.update("update tb_internacao_agendada set data_prevista = current_date - 1, reservado_ate = now() - interval '1 hour' where uuid = ?", agendamento);
        jdbc.update("update tb_leito set reservado_ate = now() - interval '1 hour' where uuid = ?", cirurgico);
        mockMvc.perform(get(ELETIVA + "?unidadeId=" + hospital.getUuid()))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].status".formatted(agendamento)).value(org.hamcrest.Matchers.contains("AGENDADA")))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].aguardandoDesfecho".formatted(agendamento)).value(org.hamcrest.Matchers.contains(true)));
        mockMvc.perform(get("/api/v1/leito/?unidadeId=" + hospital.getUuid()))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].situacao".formatted(cirurgico)).value(org.hamcrest.Matchers.contains("LIVRE")));
        acao(agendamento, "falta", "{\"profissionalMatricula\": \"EC-MED\", \"motivo\": \"Não compareceu.\"}").andExpect(status().isOk());
    }

    // ── Censo ──────────────────────────────────────────────────────────────────────────────────

    @Test
    void censoDoDiaFechaReabreEFechaOutraVersao() throws Exception {
        LocalDate ontem = LocalDate.now().minusDays(1);
        UUID l1 = leito("CE-01", TipoLeito.CLINICO);
        UUID l2 = leito("CE-02", TipoLeito.CLINICO);
        UUID l3 = leito("CE-03", TipoLeito.CLINICO);
        UUID l4 = leito("CE-04", TipoLeito.CLINICO);
        UUID l5 = leito("CE-05", TipoLeito.CLINICO);
        mockMvc.perform(post("/api/v1/leito/" + l5 + "/bloqueio").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Manutenção.\"}"))
                .andExpect(status().isOk());
        // A: desde anteontem, segue. B: entrou e saiu ontem. C: entrou ontem, segue. D: desde três dias, óbito ontem.
        internar("A", l1, ontem.minusDays(1).atTime(9, 0), null, null);
        internar("B", l2, ontem.atTime(10, 0), ontem.atTime(15, 0), "MELHORADO");
        internar("C", l3, ontem.atTime(8, 0), null, null);
        internar("D", l4, ontem.minusDays(2).atTime(9, 0), ontem.atTime(20, 0), "OBITO");

        mockMvc.perform(get(CENSO + "?unidadeId=%s&data=%s".formatted(hospital.getUuid(), ontem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechado").value(false))
                .andExpect(jsonPath("$.leitosExistentes").value(5))
                .andExpect(jsonPath("$.leitosOperacionais").value(4))
                .andExpect(jsonPath("$.internadosInicio").value(2))
                .andExpect(jsonPath("$.admissoes").value(2))
                .andExpect(jsonPath("$.altasMelhorado").value(1))
                .andExpect(jsonPath("$.obitos").value(1))
                .andExpect(jsonPath("$.internadosFim").value(2))
                .andExpect(jsonPath("$.pacienteDia").value(3))
                .andExpect(jsonPath("$.taxaOcupacao").value(75.0))
                .andExpect(jsonPath("$.pacientes", hasSize(2)))
                .andExpect(jsonPath("$.pacientes[*].leitoIdentificacao").value(org.hamcrest.Matchers.contains("CE-01", "CE-03")));

        fechamento("fechamento", LocalDate.now(), null).andExpect(status().isUnprocessableEntity());
        fechamento("fechamento", ontem, null).andExpect(status().isCreated()).andExpect(jsonPath("$.versao").value(1));
        fechamento("fechamento", ontem, null).andExpect(status().isConflict());
        mockMvc.perform(get(CENSO + "?unidadeId=%s&data=%s".formatted(hospital.getUuid(), ontem))).andExpect(jsonPath("$.fechado").value(true));
        mockMvc.perform(get(CENSO + "pendencias?unidadeId=" + hospital.getUuid()))
                .andExpect(jsonPath("$[?(@.data == '%s')]".formatted(ontem), hasSize(0)))
                .andExpect(jsonPath("$[?(@.data == '%s')]".formatted(ontem.minusDays(1)), hasSize(1)));

        fechamento("reabertura", ontem, null).andExpect(status().isBadRequest());
        fechamento("reabertura", ontem, "Alta do leito CE-02 lançada no dia errado.").andExpect(status().isOk()).andExpect(jsonPath("$.fechado").value(false));
        fechamento("fechamento", ontem, null).andExpect(status().isCreated()).andExpect(jsonPath("$.versao").value(2));
        mockMvc.perform(get(CENSO + "versoes?unidadeId=%s&data=%s".formatted(hospital.getUuid(), ontem)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].versao").value(2))
                .andExpect(jsonPath("$[0].reaberto").value(false))
                .andExpect(jsonPath("$[1].reaberto").value(true))
                .andExpect(jsonPath("$[1].motivoReabertura").value("Alta do leito CE-02 lançada no dia errado."));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private Paciente paciente(String nome) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1970, 1, 1));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private UUID leito(String identificacao, TipoLeito tipo) {
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(cirurgia);
        l.setIdentificacao(identificacao);
        l.setTipo(tipo);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(true);
        return leitoRepository.save(l).getUuid();
    }

    private String corpoAgendamento(Paciente p, LocalDate data) {
        return """
                {"pacienteId": "%s", "unidadeId": "%s", "dataPrevista": "%s", "tipoLeito": "CIRURGICO", "medicoMatricula": "EC-MED",
                 "cid": "K80.2", "procedimento": "Colecistectomia videolaparoscópica.", "previsaoDias": 2}
                """.formatted(p.getUuid(), hospital.getUuid(), data);
    }

    private UUID agendar(Paciente p, LocalDate data) throws Exception {
        String corpo = mockMvc.perform(post(ELETIVA).contentType(MediaType.APPLICATION_JSON).content(corpoAgendamento(p, data)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.uuid"));
    }

    private ResultActions acao(UUID agendamento, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(ELETIVA + agendamento + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions fechamento(String rota, LocalDate data, String motivo) throws Exception {
        return mockMvc.perform(post(CENSO + rota).contentType(MediaType.APPLICATION_JSON).content("""
                {"unidadeId": "%s", "data": "%s"%s}
                """.formatted(hospital.getUuid(), data, motivo != null ? ", \"motivo\": \"" + motivo + "\"" : "")));
    }

    /** Interna pela API e ajusta as horas da admissão e da alta direto no banco, para montar o dia do censo. */
    private void internar(String nome, UUID leito, java.time.LocalDateTime admissao, java.time.LocalDateTime alta, String tipoAlta) throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "EC-MED", "cid": "J18.9", "motivo": "Pneumonia.", "carater": "URGENCIA"}
                """.formatted(paciente(nome).getUuid(), leito))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(JsonPath.read(corpo, "$.uuid"));
        jdbc.update("update tb_internacao set admitida_em = ? where uuid = ?", Timestamp.from(admissao.atZone(ZoneId.systemDefault()).toInstant()), id);
        if (alta != null) {
            mockMvc.perform(post("/api/v1/internacao/" + id + "/alta").contentType(MediaType.APPLICATION_JSON).content("""
                    {"tipoAlta": "%s", "sumario": "Alta registrada no teste do censo.", "medicoMatricula": "EC-MED"}
                    """.formatted(tipoAlta))).andExpect(status().isOk());
            jdbc.update("update tb_internacao set alta_em = ? where uuid = ?", Timestamp.from(alta.atZone(ZoneId.systemDefault()).toInstant()), id);
        }
        assertThat(LocalTime.MIDNIGHT).isNotNull();
    }
}
