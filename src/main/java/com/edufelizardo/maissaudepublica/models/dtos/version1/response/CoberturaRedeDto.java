package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A cobertura da rede e os indicadores de pactuação (GR3, ADR-0198): o que a PPI, as referências, a fila da Regulação e a população adscrita
 * dizem juntos, por procedimento e por origem. Só agregados.
 */
public record CoberturaRedeDto(LocalDate de, LocalDate ate, boolean fechada, String abrangencia, Resumo resumo, List<Procedimento> procedimentos,
                               List<String> alertas, List<Lacuna> lacunas) {

    public record Resumo(int ofertado, int pactuado, int utilizado, int extracotas, BigDecimal percentualPactuado, BigDecimal percentualCotaUsada,
                         BigDecimal percentualExtracota, BigDecimal percentualForaDaReferencia, int cotasEsgotadasComFila, int lacunasDasRedes) {
    }

    /**
     * Por procedimento com oferta no período: a pactuação, as autorizações, a fila de hoje e as medianas de espera (em dias). O tempo até a
     * autorização conta as autorizações registradas no período.
     */
    public record Procedimento(UUID procedimentoId, String procedimentoNome, int ofertado, int pactuado, int utilizado, int extracotas,
                               BigDecimal percentualPactuado, BigDecimal percentualCotaUsada, BigDecimal percentualExtracota,
                               BigDecimal percentualForaDaReferencia, int naFila, BigDecimal esperaNaFilaDias, BigDecimal esperaAteAutorizacaoDias,
                               List<Origem> origens) {
    }

    /**
     * A origem é a beneficiária mais específica que cobre a solicitante (a unidade ou a esfera acima) ou, sem cota, a própria solicitante. A
     * população é a adscrita às equipes da unidade e, na esfera, a das unidades abaixo dela.
     */
    public record Origem(UUID unidadeId, String unidadeNome, String unidadeTipo, long populacaoAdscrita, Integer cota, BigDecimal cotaPorMil,
                         int solicitadas, int autorizadas, int extracotas, int aguardando, Integer saldo, boolean comReferencia, List<String> alertas) {
    }

    public record Lacuna(String rede, String lacuna) {
    }
}
