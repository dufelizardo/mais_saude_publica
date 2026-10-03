package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AlertaAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regras de detecção dos alertas da auditoria (ADR-0096): os eventos são gravados direto na trilha, com o horário que a
 * regra precisa, e a detecção roda com um "agora" fixo.
 */
@SpringBootTest
@ActiveProfiles("test")
class DeteccaoAlertasAuditoriaTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final String PREFIXO = "Alerta Teste ";
    private static final String CPF_RECUSAS = "90100000001";
    private static final String CPF_POUCAS = "90100000002";
    private static final String CPF_MASSA = "90100000003";
    private static final String CPF_NOITE_UBS = "90100000004";
    private static final String CPF_NOITE_HOSPITAL = "90100000005";
    private static final String CPF_JUSTIFICADO = "90100000006";
    private static final String CPF_LOGIN = "90100000007";
    private static final String IP_LOGIN = "10.99.0.7";
    private static final List<String> CPFS = List.of(CPF_RECUSAS, CPF_POUCAS, CPF_MASSA, CPF_NOITE_UBS, CPF_NOITE_HOSPITAL,
            CPF_JUSTIFICADO, CPF_LOGIN);

    @Autowired
    private DeteccaoAlertasAuditoriaService deteccao;

    @Autowired
    private EventoAuditoriaRepository eventoRepository;

    @Autowired
    private AlertaAuditoriaRepository alertaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private JdbcTemplate jdbc;

    private Instant inicio;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude hospital;

    @BeforeEach
    void seed() {
        limpar();
        inicio = Instant.now();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL);
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
            jdbc.update("delete from tb_alerta_auditoria where sujeito = ?", cpf);
        }
        jdbc.update("delete from tb_evento_auditoria where origem_ip = ?", IP_LOGIN);
        jdbc.update("delete from tb_alerta_auditoria where sujeito = ?", "IP " + IP_LOGIN);
        if (inicio != null) {
            // Alertas que a detecção abriu sobre eventos de outros testes no mesmo banco.
            jdbc.update("delete from tb_alerta_auditoria where detectado_em >= ?", Timestamp.from(inicio));
        }
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void evento(String cpf, AcaoAuditoria acao, ResultadoAuditoria resultado, Instant em, UUID unidade, UUID paciente,
                        String ip) {
        EventoAuditoria e = new EventoAuditoria();
        e.setOcorridoEm(em);
        e.setUsuarioCpf(cpf);
        e.setAcao(acao);
        e.setResultado(resultado);
        e.setRecurso("TESTE");
        e.setMetodo("GET");
        e.setRota("/api/v1/teste");
        e.setStatusHttp(resultado == ResultadoAuditoria.NEGADO ? 403 : 200);
        e.setUnidadeId(unidade);
        e.setPacienteId(paciente);
        e.setOrigemIp(ip);
        eventoRepository.save(e);
    }

    private List<AlertaAuditoria> alertasDe(String sujeito, TipoAlertaAuditoria tipo) {
        return alertaRepository.findAllByOrderByDetectadoEmDesc().stream()
                .filter(a -> a.getSujeito().equals(sujeito) && a.getTipo() == tipo).toList();
    }

    @Test
    void recusasSeguidasAbremUmAlertaQueSomaEnquantoContinua() {
        Instant agora = Instant.now();
        for (int i = 0; i < 10; i++) {
            evento(CPF_RECUSAS, AcaoAuditoria.LEITURA, ResultadoAuditoria.NEGADO, agora.minusSeconds(600 - i * 30L), ubs.getUuid(), null, null);
        }
        for (int i = 0; i < 9; i++) {
            evento(CPF_POUCAS, AcaoAuditoria.ALTERACAO, ResultadoAuditoria.NEGADO, agora.minusSeconds(300 - i * 10L), ubs.getUuid(), null, null);
        }
        deteccao.detectar(agora);

        List<AlertaAuditoria> alertas = alertasDe(CPF_RECUSAS, TipoAlertaAuditoria.RECUSAS_SEGUIDAS);
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getSeveridade()).isEqualTo(SeveridadeAlertaAuditoria.ALTA);
        assertThat(alertas.get(0).getQuantidade()).isEqualTo(10);
        assertThat(alertas.get(0).getUnidadeId()).isEqualTo(ubs.getUuid());
        assertThat(alertasDe(CPF_POUCAS, TipoAlertaAuditoria.RECUSAS_SEGUIDAS)).isEmpty();

        // Continua recusando: o mesmo alerta cresce, sem abrir outro.
        Instant depois = agora.plusSeconds(120);
        for (int i = 0; i < 3; i++) {
            evento(CPF_RECUSAS, AcaoAuditoria.LEITURA, ResultadoAuditoria.NEGADO, depois.minusSeconds(60 - i * 10L), ubs.getUuid(), null, null);
        }
        assertThat(deteccao.detectar(depois)).noneMatch(a -> a.getSujeito().equals(CPF_RECUSAS));
        alertas = alertasDe(CPF_RECUSAS, TipoAlertaAuditoria.RECUSAS_SEGUIDAS);
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getQuantidade()).isEqualTo(13);
    }

    @Test
    void loginRecusadoAgrupaPorCpfOuPelaOrigem() {
        Instant agora = Instant.now();
        for (int i = 0; i < 5; i++) {
            evento(CPF_LOGIN, AcaoAuditoria.LOGIN, ResultadoAuditoria.NEGADO, agora.minusSeconds(200 - i * 20L), null, null, "10.99.0.1");
            evento(null, AcaoAuditoria.LOGIN, ResultadoAuditoria.NEGADO, agora.minusSeconds(200 - i * 20L), null, null, IP_LOGIN);
        }
        deteccao.detectar(agora);
        assertThat(alertasDe(CPF_LOGIN, TipoAlertaAuditoria.LOGIN_RECUSADO)).hasSize(1);
        List<AlertaAuditoria> porIp = alertasDe("IP " + IP_LOGIN, TipoAlertaAuditoria.LOGIN_RECUSADO);
        assertThat(porIp).hasSize(1);
        assertThat(porIp.get(0).getUsuarioCpf()).isNull();
        jdbc.update("delete from tb_evento_auditoria where origem_ip = ?", "10.99.0.1");
    }

    @Test
    void leituraDeMuitosPacientesDiferentesEmUmaHora() {
        Instant agora = Instant.now();
        for (int i = 0; i < 30; i++) {
            evento(CPF_MASSA, AcaoAuditoria.LEITURA, ResultadoAuditoria.PERMITIDO, agora.minusSeconds(3000 - i * 60L), null, UUID.randomUUID(), null);
        }
        deteccao.detectar(agora);
        List<AlertaAuditoria> alertas = alertasDe(CPF_MASSA, TipoAlertaAuditoria.LEITURA_EM_MASSA);
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getQuantidade()).isEqualTo(30);
    }

    @Test
    void leituraDeMadrugadaForaDeUnidade24hUmAlertaPorNoite() {
        LocalDate hoje = LocalDate.now(FUSO);
        Instant agora = hoje.atTime(LocalTime.of(7, 0)).atZone(FUSO).toInstant();
        Instant onzeEDez = hoje.minusDays(1).atTime(LocalTime.of(23, 10)).atZone(FUSO).toInstant();
        for (Instant em : List.of(onzeEDez, onzeEDez.plus(Duration.ofMinutes(30)), onzeEDez.plus(Duration.ofMinutes(110)))) {
            evento(CPF_NOITE_UBS, AcaoAuditoria.LEITURA, ResultadoAuditoria.PERMITIDO, em, ubs.getUuid(), UUID.randomUUID(), null);
            evento(CPF_NOITE_HOSPITAL, AcaoAuditoria.LEITURA, ResultadoAuditoria.PERMITIDO, em, hospital.getUuid(), UUID.randomUUID(), null);
        }
        // De dia não conta.
        evento(CPF_NOITE_UBS, AcaoAuditoria.LEITURA, ResultadoAuditoria.PERMITIDO,
                hoje.minusDays(1).atTime(LocalTime.of(15, 0)).atZone(FUSO).toInstant(), ubs.getUuid(), UUID.randomUUID(), null);

        deteccao.detectar(agora);
        List<AlertaAuditoria> alertas = alertasDe(CPF_NOITE_UBS, TipoAlertaAuditoria.LEITURA_FORA_DO_HORARIO);
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getQuantidade()).isEqualTo(3);
        assertThat(alertas.get(0).getSeveridade()).isEqualTo(SeveridadeAlertaAuditoria.MEDIA);
        assertThat(alertas.get(0).getUnidadeId()).isEqualTo(ubs.getUuid());
        assertThat(alertasDe(CPF_NOITE_HOSPITAL, TipoAlertaAuditoria.LEITURA_FORA_DO_HORARIO)).isEmpty();

        // Rodar de novo na mesma noite não repete.
        deteccao.detectar(agora.plusSeconds(300));
        assertThat(alertasDe(CPF_NOITE_UBS, TipoAlertaAuditoria.LEITURA_FORA_DO_HORARIO)).hasSize(1);
    }

    @Test
    void cadaAcessoJustificadoViraUmAlertaDeRevisao() {
        Instant agora = Instant.now();
        UUID paciente = UUID.randomUUID();
        evento(CPF_JUSTIFICADO, AcaoAuditoria.ACESSO_JUSTIFICADO, ResultadoAuditoria.PERMITIDO, agora.minusSeconds(100), null, paciente, null);
        deteccao.detectar(agora);
        deteccao.detectar(agora.plusSeconds(300));
        List<AlertaAuditoria> alertas = alertasDe(CPF_JUSTIFICADO, TipoAlertaAuditoria.ACESSO_JUSTIFICADO);
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getPacienteId()).isEqualTo(paciente);
        assertThat(alertas.get(0).getStatus()).isEqualTo(StatusAlertaAuditoria.ABERTO);
    }
}
