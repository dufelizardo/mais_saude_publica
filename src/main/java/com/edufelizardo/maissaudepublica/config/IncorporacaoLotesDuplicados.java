package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
import com.edufelizardo.maissaudepublica.services.version1.MovimentacaoFarmaciaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Garante uma remessa (medicamento, número e validade) por unidade (ADR-0060). Roda na subida da
 * aplicação, depois do saldo inicial do livro ({@link SaldoInicialFarmaciaBackfill}):
 *
 * <ol>
 *   <li>Lotes ativos da mesma remessa na mesma unidade — criados antes desta regra existir — são
 *   incorporados ao mais antigo: o saldo de cada duplicado sai dele ({@code INCORPORACAO_SAIDA}) e
 *   entra no lote que fica ({@code INCORPORACAO_ENTRADA}), e o duplicado é marcado como incorporado.
 *   O histórico dos dois extratos é preservado.</li>
 *   <li>Cria, se ainda não existe, o índice único parcial que impede duas remessas ativas iguais na
 *   mesma unidade — segunda linha de defesa, além da trava da unidade nos serviços.</li>
 * </ol>
 *
 * <p>Idempotente: sem duplicados, não lança nada.
 */
@Component
@Order(2)
@Slf4j
public class IncorporacaoLotesDuplicados implements ApplicationRunner {

    static final String INDICE_REMESSA_UNICA = "uk_lote_remessa_ativa_por_unidade";

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private record Remessa(Object medicamentoId, Object unidadeId, String numeroLote, Object validade) {
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<Remessa, List<Lote>> porRemessa = loteRepository.findByLoteIncorporadorIsNull().stream()
                .collect(Collectors.groupingBy(l -> new Remessa(l.getMedicamento().getUuid(), l.getUnidade().getUuid(),
                        l.getNumeroLote(), l.getValidade())));

        int incorporados = 0;
        for (List<Lote> grupo : porRemessa.values()) {
            if (grupo.size() < 2) {
                continue;
            }
            List<Lote> ordenados = grupo.stream()
                    .sorted(Comparator.comparing(this::primeiroLancamento).thenComparing(Lote::getUuid))
                    .toList();
            // Trava em ordem crescente de id, como as demais movimentações (ADR-0059).
            ordenados.stream().map(Lote::getUuid).sorted()
                    .forEach(id -> loteRepository.findByIdParaMovimentar(id).orElseThrow());

            Lote fica = ordenados.get(0);
            for (Lote duplicado : ordenados.subList(1, ordenados.size())) {
                incorporar(duplicado, fica);
                incorporados++;
            }
        }
        if (incorporados > 0) {
            log.warn("Farmácia: {} lote(s) duplicado(s) da mesma remessa incorporado(s) ao lote mais antigo (ADR-0060).",
                    incorporados);
        }

        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + INDICE_REMESSA_UNICA
                + " ON tb_lote (medicamento_id, unidade_id, numero_lote, validade) WHERE lote_incorporador_id IS NULL");
    }

    private void incorporar(Lote duplicado, Lote fica) {
        int saldo = duplicado.getQuantidade();
        String justificativa = "Lote duplicado da mesma remessa na mesma unidade: saldo levado ao lote " + fica.getUuid()
                + " (ADR-0060).";
        movimentacaoFarmaciaService.lancar(duplicado, TipoMovimentacaoFarmacia.INCORPORACAO_SAIDA, -saldo, null, null,
                justificativa, null);
        movimentacaoFarmaciaService.lancar(fica, TipoMovimentacaoFarmacia.INCORPORACAO_ENTRADA, saldo, null, null,
                "Saldo do lote duplicado " + duplicado.getUuid() + " incorporado (ADR-0060).", null);
        duplicado.setLoteIncorporador(fica);
        duplicado.setIncorporadoEm(Instant.now());
        loteRepository.save(duplicado);
    }

    private Instant primeiroLancamento(Lote lote) {
        List<MovimentacaoFarmacia> extrato = movimentacaoRepository.findByLote_UuidOrderByRegistradoEmAsc(lote.getUuid());
        return extrato.isEmpty() ? Instant.MAX : extrato.get(0).getRegistradoEm();
    }
}
