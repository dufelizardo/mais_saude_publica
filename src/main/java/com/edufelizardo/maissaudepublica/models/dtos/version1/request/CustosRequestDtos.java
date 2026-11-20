package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CriterioRateio;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCentroCusto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/** Os pedidos dos centros de custo e da apuração, fatias F3 (ADR-0220) e F4 (rateio, ADR-0221). */
public final class CustosRequestDtos {

    private CustosRequestDtos() {
    }

    /**
     * O centro de custo de um setor; sem nome, o do setor; sem tipo, o do tipo do setor. Sem critério, o padrão do tipo (o produtivo não
     * rateia); a área é a base do critério AREA.
     */
    public record NovoCentro(@NotNull(message = "Informe o setor.") UUID setorId,
                             @Size(max = 150, message = "O nome vai até 150 caracteres.") String nome,
                             TipoCentroCusto tipo,
                             CriterioRateio criterioRateio,
                             @Positive(message = "A área é maior que zero.") @Digits(integer = 8, fraction = 2, message = "A área vai até duas casas.") BigDecimal areaM2) {
    }

    /** O nome, o tipo e se está ativo; o geral da unidade não se desativa. */
    public record AtualizacaoCentro(@NotBlank(message = "Informe o nome.") @Size(max = 150, message = "O nome vai até 150 caracteres.") String nome,
                                    @NotNull(message = "Informe o tipo.") TipoCentroCusto tipo,
                                    @NotNull(message = "Informe se está ativo.") Boolean ativo,
                                    CriterioRateio criterioRateio,
                                    @Positive(message = "A área é maior que zero.") @Digits(integer = 8, fraction = 2, message = "A área vai até duas casas.") BigDecimal areaM2) {
    }

    public record Apurar(@NotBlank(message = "Informe a competência.")
                         @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "A competência é AAAA-MM.") String competencia) {
    }
}
