package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaFarmaciaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ajustes da passagem da transferência imediata (ADR-0059) para a transferência em duas etapas
 * (ADR-0061), na subida da aplicação. Idempotente.
 *
 * <ol>
 *   <li>{@code lote_destino_id} deixa de ser obrigatório — o lote de destino só existe depois do
 *   recebimento. O Hibernate ({@code ddl-auto=update}) acrescenta colunas mas não relaxa uma coluna
 *   que já nasceu {@code NOT NULL}, por isso o ajuste é feito aqui.</li>
 *   <li>Transferências feitas antes das duas etapas, que já lançaram a entrada no destino, recebem
 *   status {@code RECEBIDA} com os dados do próprio envio (unidade do lote de destino, quantidade
 *   enviada, mesmo profissional e hora).</li>
 * </ol>
 */
@Component
@Order(3)
@Slf4j
public class AjusteTransferenciasFarmacia implements ApplicationRunner {

    @Autowired
    private TransferenciaFarmaciaRepository transferenciaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS tb_transferencia_farmacia ALTER COLUMN lote_destino_id DROP NOT NULL");

        List<TransferenciaFarmacia> anteriores = transferenciaRepository.findByStatusIsNull();
        for (TransferenciaFarmacia t : anteriores) {
            t.setStatus(StatusTransferenciaFarmacia.RECEBIDA);
            if (t.getLoteDestino() != null) {
                t.setUnidadeDestino(t.getLoteDestino().getUnidade());
            }
            t.setQuantidadeRecebida(t.getQuantidade());
            t.setProfissionalRecebimento(t.getProfissional());
            t.setRecebidoEm(t.getRegistradoEm());
            t.setRecebidoPorCpf(t.getRegistradoPorCpf());
        }
        transferenciaRepository.saveAll(anteriores);
        if (!anteriores.isEmpty()) {
            log.info("Farmácia: {} transferência(s) imediata(s) anteriores marcadas como RECEBIDA (ADR-0061).",
                    anteriores.size());
        }
    }
}
