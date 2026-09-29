package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Lotes cadastrados antes do livro de movimentação (ADR-0057) não têm lançamento nenhum. Na subida,
 * cada um ganha um SALDO_INICIAL com a quantidade que já tinha, para o extrato começar de um saldo
 * conhecido. Idempotente: lotes que já têm qualquer lançamento são ignorados.
 */
@Component
@Order(1)
@Slf4j
public class SaldoInicialFarmaciaBackfill implements ApplicationRunner {

    private static final String JUSTIFICATIVA =
            "Saldo que o lote já tinha antes do livro de movimentação existir (ADR-0057).";

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoRepository;

    @Override
    public void run(ApplicationArguments args) {
        int criados = 0;
        for (Lote lote : loteRepository.findAll()) {
            if (movimentacaoRepository.existsByLote_Uuid(lote.getUuid())) {
                continue;
            }
            movimentacaoRepository.save(new MovimentacaoFarmacia(null, lote, TipoMovimentacaoFarmacia.SALDO_INICIAL,
                    lote.getQuantidade(), lote.getQuantidade(), null, JUSTIFICATIVA, null, null, null, Instant.now(), null));
            criados++;
        }
        if (criados > 0) {
            log.info("Livro de movimentação: saldo inicial registrado para {} lote(s) anteriores.", criados);
        }
    }
}
