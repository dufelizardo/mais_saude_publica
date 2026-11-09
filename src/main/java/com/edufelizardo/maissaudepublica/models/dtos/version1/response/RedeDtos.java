package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/** Respostas da Gestão da Rede, oferta e PPI (ADR-0179). */
public final class RedeDtos {

    private RedeDtos() {
    }

    /** A cota da beneficiária, com o que já foi usado e as extracotas autorizadas para ela. */
    public record Cota(UUID uuid, UUID beneficiariaId, String beneficiariaNome, String beneficiariaTipo, int quantidade, int utilizado, int saldo) {
    }

    public record Oferta(UUID uuid, YearMonth competencia, UUID executanteId, String executanteNome, UUID procedimentoId, String procedimentoNome,
                         int quantidade, int pactuado, int semPactuar, int utilizado, int extracotas, String observacao, List<Cota> cotas) {
    }

    /** As ofertas da competência e os totais. */
    public record Painel(YearMonth competencia, int ofertado, int pactuado, int utilizado, int extracotas, List<Oferta> ofertas) {
    }

    /**
     * O que a autorização vai encontrar: {@code controlada} quando há oferta cadastrada para a executante, o procedimento e o mês; a cota
     * mais específica que cobre a solicitante e o saldo dela.
     */
    public record Saldo(boolean controlada, UUID ofertaId, UUID cotaId, String beneficiariaNome, Integer cota, Integer utilizado, Integer saldo) {
    }

    public record Copia(YearMonth origem, YearMonth destino, int ofertasCopiadas, int cotasCopiadas, int ofertasJaExistentes) {
    }
}
