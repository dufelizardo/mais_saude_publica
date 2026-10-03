package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Política de retenção da trilha de auditoria (ADR-0082). Eventos mais antigos que o prazo
 * ({@code app.auditoria.retencao-anos}, padrão 20 — o mesmo prazo de guarda do prontuário, Lei 13.787/2018) são
 * apagados por uma rotina diária, em lotes. A própria rotina deixa um evento na trilha dizendo quantos apagou e
 * até que data. Prazo 0 = guarda indefinida (a rotina não apaga nada).
 *
 * <p>É a única exceção à trilha que "só cresce" (ADR-0070): o prazo é decidido aqui, e não por quem consulta.
 */
@Service
@Slf4j
public class RetencaoAuditoriaService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int LOTE = 10_000;

    @Value("${app.auditoria.retencao-anos:20}")
    private int retencaoAnos;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditoriaService auditoriaService;

    public int retencaoAnos() {
        return retencaoAnos;
    }

    /** Instante a partir do qual os eventos são guardados; nulo com guarda indefinida. */
    public Instant guardadosDesde() {
        return retencaoAnos <= 0 ? null : ZonedDateTime.now(FUSO).minusYears(retencaoAnos).toInstant();
    }

    @Scheduled(cron = "${app.auditoria.retencao.cron:0 30 3 * * *}", zone = "America/Sao_Paulo")
    public void rotinaDiaria() {
        aplicar();
    }

    /** Apaga, em lotes, os eventos anteriores ao prazo. Devolve quantos apagou. */
    public long aplicar() {
        Instant limite = guardadosDesde();
        if (limite == null) {
            return 0;
        }
        long apagados = 0;
        int lote;
        do {
            lote = jdbcTemplate.update("""
                    delete from tb_evento_auditoria where uuid in (
                        select uuid from tb_evento_auditoria where ocorrido_em < ? limit ?)
                    """, Timestamp.from(limite), LOTE);
            apagados += lote;
        } while (lote == LOTE);
        if (apagados > 0) {
            registrar(apagados, limite);
            log.info("Retenção da auditoria: {} eventos anteriores a {} apagados.", apagados, limite);
        }
        return apagados;
    }

    private void registrar(long apagados, Instant limite) {
        EventoAuditoria e = new EventoAuditoria();
        e.setOcorridoEm(Instant.now());
        e.setAcao(AcaoAuditoria.EXCLUSAO);
        e.setResultado(ResultadoAuditoria.PERMITIDO);
        e.setRecurso("AUDITORIA");
        e.setMetodo("ROTINA");
        e.setRota("retencao-da-auditoria");
        e.setStatusHttp(200);
        e.setDetalhe("Retenção de " + retencaoAnos + " anos: " + apagados + " eventos anteriores a "
                + limite.atZone(FUSO).toLocalDate() + " apagados.");
        auditoriaService.registrarSemFalhar(e);
    }
}
