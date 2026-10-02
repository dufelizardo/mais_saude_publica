package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AlertaAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Detecção periódica de padrões suspeitos na trilha de auditoria (ADR-0096). Roda a cada poucos minutos sobre os
 * eventos recentes, por consultas agregadas, sem pesar na gravação da trilha. Cada regra gera no máximo um alerta por
 * episódio: as de janela deslizante somam no alerta aberto do mesmo sujeito, e as demais usam uma chave única.
 */
@Service
@Slf4j
public class DeteccaoAlertasAuditoriaService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    /** Quanto tempo para trás as regras de chave única olham: cobre a rotina parada por algumas horas. */
    private static final Duration RETROSPECTO = Duration.ofHours(24);

    @Value("${app.auditoria.alertas.recusas.limite:10}")
    private int recusasLimite;
    @Value("${app.auditoria.alertas.recusas.janela-minutos:15}")
    private int recusasJanela;
    @Value("${app.auditoria.alertas.login.limite:5}")
    private int loginLimite;
    @Value("${app.auditoria.alertas.login.janela-minutos:15}")
    private int loginJanela;
    @Value("${app.auditoria.alertas.massa.limite-pacientes:30}")
    private int massaLimite;
    @Value("${app.auditoria.alertas.massa.janela-minutos:60}")
    private int massaJanela;
    @Value("${app.auditoria.alertas.fora-do-horario.inicio:22}")
    private int noiteInicio;
    @Value("${app.auditoria.alertas.fora-do-horario.fim:6}")
    private int noiteFim;
    @Value("${app.auditoria.alertas.fora-do-horario.limite:3}")
    private int noiteLimite;
    @Value("${app.auditoria.alertas.unidades-24h:HOSPITAL,UPA}")
    private Set<TipoUnidadeDeSaude> unidades24h;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AlertaAuditoriaRepository alertaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Autowired
    private AvisoAlertasAuditoria aviso;

    @Scheduled(cron = "${app.auditoria.alertas.cron:0 */5 * * * *}", zone = "America/Sao_Paulo")
    public void rotina() {
        try {
            detectar(Instant.now());
        } catch (RuntimeException ex) {
            log.warn("Detecção de alertas da auditoria falhou: {}", ex.getMessage());
        }
    }

    /** Roda todas as regras até {@code agora}. Devolve os alertas novos (os somados a um aberto não contam). */
    @Transactional
    public List<AlertaAuditoria> detectar(Instant agora) {
        List<AlertaAuditoria> novos = new ArrayList<>();
        recusasSeguidas(agora, novos);
        loginRecusado(agora, novos);
        leituraEmMassa(agora, novos);
        foraDoHorario(agora, novos);
        acessoJustificado(agora, novos);
        novos.stream().filter(a -> a.getSeveridade() == SeveridadeAlertaAuditoria.ALTA).forEach(aviso::avisar);
        return novos;
    }

    // ── Regras de janela deslizante ────────────────────────────────────────────────────────────────

    private void recusasSeguidas(Instant agora, List<AlertaAuditoria> novos) {
        Instant desde = agora.minus(Duration.ofMinutes(recusasJanela));
        jdbc.query("""
                select usuario_cpf, count(*) n, min(ocorrido_em) primeiro, max(ocorrido_em) ultimo,
                       count(distinct unidade_id) unidades, min(cast(unidade_id as varchar)) unidade
                  from tb_evento_auditoria
                 where resultado = 'NEGADO' and acao <> 'LOGIN' and acao <> 'RECUPERACAO_DE_SENHA'
                   and usuario_cpf is not null and ocorrido_em > ? and ocorrido_em <= ?
                 group by usuario_cpf having count(*) >= ?
                """, rs -> {
            String cpf = rs.getString("usuario_cpf");
            int n = rs.getInt("n");
            UUID unidade = rs.getInt("unidades") == 1 ? UUID.fromString(rs.getString("unidade")) : null;
            janela(TipoAlertaAuditoria.RECUSAS_SEGUIDAS, SeveridadeAlertaAuditoria.ALTA, cpf, cpf,
                    unidade != null ? unidade : unidadeDoUsuario(cpf), rs.getTimestamp("primeiro").toInstant(),
                    rs.getTimestamp("ultimo").toInstant(), n, Duration.ofMinutes(recusasJanela), agora,
                    n + " acessos recusados (sem permissão ou fora do escopo) em até " + recusasJanela + " minutos.", novos);
        }, Timestamp.from(desde), Timestamp.from(agora), recusasLimite);
    }

    private void loginRecusado(Instant agora, List<AlertaAuditoria> novos) {
        Instant desde = agora.minus(Duration.ofMinutes(loginJanela));
        jdbc.query("""
                select coalesce(usuario_cpf, 'IP ' || coalesce(origem_ip, 'desconhecido')) sujeito, max(usuario_cpf) cpf,
                       count(*) n, min(ocorrido_em) primeiro, max(ocorrido_em) ultimo, count(distinct origem_ip) origens
                  from tb_evento_auditoria
                 where acao = 'LOGIN' and resultado = 'NEGADO' and ocorrido_em > ? and ocorrido_em <= ?
                 group by coalesce(usuario_cpf, 'IP ' || coalesce(origem_ip, 'desconhecido')) having count(*) >= ?
                """, rs -> {
            String sujeito = rs.getString("sujeito");
            String cpf = rs.getString("cpf");
            int n = rs.getInt("n");
            janela(TipoAlertaAuditoria.LOGIN_RECUSADO, SeveridadeAlertaAuditoria.ALTA, sujeito, cpf,
                    cpf != null ? unidadeDoUsuario(cpf) : null, rs.getTimestamp("primeiro").toInstant(),
                    rs.getTimestamp("ultimo").toInstant(), n, Duration.ofMinutes(loginJanela), agora,
                    n + " logins recusados em até " + loginJanela + " minutos, de " + rs.getInt("origens")
                            + " origem(ns). Pode ser tentativa de descobrir a senha.", novos);
        }, Timestamp.from(desde), Timestamp.from(agora), loginLimite);
    }

    private void leituraEmMassa(Instant agora, List<AlertaAuditoria> novos) {
        Instant desde = agora.minus(Duration.ofMinutes(massaJanela));
        jdbc.query("""
                select usuario_cpf, count(distinct paciente_id) n, min(ocorrido_em) primeiro, max(ocorrido_em) ultimo
                  from tb_evento_auditoria
                 where acao = 'LEITURA' and resultado = 'PERMITIDO' and paciente_id is not null
                   and usuario_cpf is not null and ocorrido_em > ? and ocorrido_em <= ?
                 group by usuario_cpf having count(distinct paciente_id) >= ?
                """, rs -> {
            String cpf = rs.getString("usuario_cpf");
            int n = rs.getInt("n");
            janela(TipoAlertaAuditoria.LEITURA_EM_MASSA, SeveridadeAlertaAuditoria.ALTA, cpf, cpf, unidadeDoUsuario(cpf),
                    rs.getTimestamp("primeiro").toInstant(), rs.getTimestamp("ultimo").toInstant(), n,
                    Duration.ofMinutes(massaJanela), agora,
                    "Dados de " + n + " pacientes diferentes abertos em até " + massaJanela + " minutos.", novos);
        }, Timestamp.from(desde), Timestamp.from(agora), massaLimite);
    }

    /**
     * Soma no alerta aberto do mesmo sujeito enquanto o padrão continua (último evento dentro de duas janelas); senão,
     * abre outro. A quantidade fica com o maior valor visto numa janela.
     */
    private void janela(TipoAlertaAuditoria tipo, SeveridadeAlertaAuditoria severidade, String sujeito, String cpf,
                        UUID unidade, Instant primeiro, Instant ultimo, int quantidade, Duration janela, Instant agora,
                        String descricao, List<AlertaAuditoria> novos) {
        Optional<AlertaAuditoria> aberto = alertaRepository
                .findFirstByTipoAndSujeitoAndStatusAndUltimoEventoEmAfterOrderByUltimoEventoEmDesc(
                        tipo, sujeito, StatusAlertaAuditoria.ABERTO, agora.minus(janela.multipliedBy(2)));
        if (aberto.isPresent()) {
            AlertaAuditoria a = aberto.get();
            if (ultimo.isAfter(a.getUltimoEventoEm())) {
                a.setUltimoEventoEm(ultimo);
                a.setAtualizadoEm(agora);
            }
            if (quantidade > a.getQuantidade()) {
                a.setQuantidade(quantidade);
                a.setDescricao(descricao);
                a.setAtualizadoEm(agora);
            }
            alertaRepository.save(a);
            return;
        }
        String chave = tipo + ":" + sujeito + ":" + primeiro.toEpochMilli();
        if (!alertaRepository.existsByChave(chave)) {
            novos.add(criar(tipo, severidade, chave, sujeito, cpf, unidade, null, primeiro, ultimo, quantidade, descricao, agora));
        }
    }

    // ── Regras de chave única ──────────────────────────────────────────────────────────────────────

    private record Leitura(String cpf, UUID unidade, Instant em) {
    }

    /** Leituras de madrugada; cada usuário gera um alerta por noite, a partir do limite. */
    private void foraDoHorario(Instant agora, List<AlertaAuditoria> novos) {
        List<Leitura> leituras = jdbc.query("""
                select usuario_cpf, unidade_id, ocorrido_em from tb_evento_auditoria
                 where acao = 'LEITURA' and resultado = 'PERMITIDO' and usuario_cpf is not null
                   and ocorrido_em > ? and ocorrido_em <= ?
                   and (extract(hour from ocorrido_em at time zone 'America/Sao_Paulo') >= ?
                        or extract(hour from ocorrido_em at time zone 'America/Sao_Paulo') < ?)
                """, (rs, i) -> new Leitura(rs.getString("usuario_cpf"),
                        rs.getString("unidade_id") == null ? null : UUID.fromString(rs.getString("unidade_id")),
                        rs.getTimestamp("ocorrido_em").toInstant()),
                Timestamp.from(agora.minus(RETROSPECTO)), Timestamp.from(agora), noiteInicio, noiteFim);
        if (leituras.isEmpty()) {
            return;
        }
        Map<UUID, TipoUnidadeDeSaude> tipos = new HashMap<>();
        unidadeDeSaudeRepository.findAllById(leituras.stream().map(Leitura::unidade).filter(u -> u != null).collect(Collectors.toSet()))
                .forEach(u -> tipos.put(u.getUuid(), u.getTipo()));
        Map<String, Boolean> plantonista = new HashMap<>();
        Map<String, List<Leitura>> porNoite = new LinkedHashMap<>();
        for (Leitura l : leituras) {
            boolean em24h = l.unidade() != null
                    ? unidades24h.contains(tipos.get(l.unidade()))
                    // Sem unidade no evento (ex.: prontuário): vale onde a pessoa tem acesso.
                    : plantonista.computeIfAbsent(l.cpf(), this::temAcessoEmUnidade24h);
            if (!em24h) {
                porNoite.computeIfAbsent(l.cpf() + ":" + noite(l.em()), k -> new ArrayList<>()).add(l);
            }
        }
        porNoite.forEach((k, ls) -> {
            if (ls.size() < noiteLimite) {
                return;
            }
            String chave = TipoAlertaAuditoria.LEITURA_FORA_DO_HORARIO + ":" + k;
            String cpf = ls.get(0).cpf();
            Instant primeiro = ls.stream().map(Leitura::em).min(Instant::compareTo).orElseThrow();
            Instant ultimo = ls.stream().map(Leitura::em).max(Instant::compareTo).orElseThrow();
            Optional<AlertaAuditoria> existente = alertaRepository.findFirstByTipoAndSujeitoAndStatusAndUltimoEventoEmAfterOrderByUltimoEventoEmDesc(
                    TipoAlertaAuditoria.LEITURA_FORA_DO_HORARIO, cpf, StatusAlertaAuditoria.ABERTO, primeiro.minus(Duration.ofHours(12)));
            String descricao = ls.size() + " leituras de dado de saúde entre " + noiteInicio + "h e " + noiteFim
                    + "h, fora de unidade que funciona 24 horas.";
            if (alertaRepository.existsByChave(chave)) {
                existente.filter(a -> a.getChave().equals(chave) && ls.size() > a.getQuantidade()).ifPresent(a -> {
                    a.setQuantidade(ls.size());
                    a.setUltimoEventoEm(ultimo);
                    a.setDescricao(descricao);
                    a.setAtualizadoEm(agora);
                    alertaRepository.save(a);
                });
                return;
            }
            UUID unidade = ls.stream().map(Leitura::unidade).filter(u -> u != null).findFirst().orElse(unidadeDoUsuario(cpf));
            novos.add(criar(TipoAlertaAuditoria.LEITURA_FORA_DO_HORARIO, SeveridadeAlertaAuditoria.MEDIA, chave, cpf, cpf,
                    unidade, null, primeiro, ultimo, ls.size(), descricao, agora));
        });
    }

    /** Todo acesso justificado ao prontuário vira um alerta de revisão para a supervisão. */
    private void acessoJustificado(Instant agora, List<AlertaAuditoria> novos) {
        jdbc.query("""
                select uuid, usuario_cpf, paciente_id, ocorrido_em, detalhe from tb_evento_auditoria
                 where acao = 'ACESSO_JUSTIFICADO' and resultado = 'PERMITIDO' and ocorrido_em > ? and ocorrido_em <= ?
                """, rs -> {
            String chave = TipoAlertaAuditoria.ACESSO_JUSTIFICADO + ":" + rs.getString("uuid");
            if (alertaRepository.existsByChave(chave)) {
                return;
            }
            String cpf = rs.getString("usuario_cpf");
            Instant em = rs.getTimestamp("ocorrido_em").toInstant();
            String detalhe = rs.getString("detalhe");
            String descricao = "Prontuário aberto sem vínculo assistencial, por acesso justificado. Revise o motivo."
                    + (detalhe != null && !detalhe.isBlank() ? " " + detalhe : "");
            novos.add(criar(TipoAlertaAuditoria.ACESSO_JUSTIFICADO, SeveridadeAlertaAuditoria.MEDIA, chave,
                    cpf != null ? cpf : "desconhecido", cpf, cpf != null ? unidadeDoUsuario(cpf) : null,
                    rs.getString("paciente_id") == null ? null : UUID.fromString(rs.getString("paciente_id")),
                    em, em, 1, descricao.length() > 500 ? descricao.substring(0, 500) : descricao, agora));
        }, Timestamp.from(agora.minus(RETROSPECTO)), Timestamp.from(agora));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private AlertaAuditoria criar(TipoAlertaAuditoria tipo, SeveridadeAlertaAuditoria severidade, String chave, String sujeito,
                                  String cpf, UUID unidade, UUID paciente, Instant primeiro, Instant ultimo, int quantidade,
                                  String descricao, Instant agora) {
        AlertaAuditoria a = new AlertaAuditoria();
        a.setTipo(tipo);
        a.setSeveridade(severidade);
        a.setStatus(StatusAlertaAuditoria.ABERTO);
        a.setChave(chave.length() > 200 ? chave.substring(0, 200) : chave);
        a.setSujeito(sujeito.length() > 100 ? sujeito.substring(0, 100) : sujeito);
        a.setUsuarioCpf(cpf);
        a.setUnidadeId(unidade);
        a.setPacienteId(paciente);
        a.setPrimeiroEventoEm(primeiro);
        a.setUltimoEventoEm(ultimo);
        a.setQuantidade(quantidade);
        a.setDescricao(descricao);
        a.setDetectadoEm(agora);
        a.setAtualizadoEm(agora);
        return alertaRepository.save(a);
    }

    /** A noite de um instante: a data em que ela começou (a madrugada conta para o dia anterior). */
    private LocalDate noite(Instant em) {
        ZonedDateTime local = em.atZone(FUSO);
        return local.getHour() < noiteFim ? local.toLocalDate().minusDays(1) : local.toLocalDate();
    }

    /** A unidade do alerta é a do acesso de quem foi alertado, para quem audita aquela unidade ver. */
    private UUID unidadeDoUsuario(String cpf) {
        return autorizacaoService.vigentes(cpf).stream().map(AtribuicaoAcesso::getUnidade).filter(u -> u != null)
                .map(UnidadeDeSaude::getUuid).findFirst().orElse(null);
    }

    private boolean temAcessoEmUnidade24h(String cpf) {
        return autorizacaoService.vigentes(cpf).stream().map(AtribuicaoAcesso::getUnidade).filter(u -> u != null)
                .anyMatch(u -> unidades24h.contains(u.getTipo()));
    }
}
