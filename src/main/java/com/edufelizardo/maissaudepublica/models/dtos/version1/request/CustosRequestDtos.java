package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoCentroCusto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Os pedidos dos centros de custo e da apuração, fatia F3 (ADR-0220). */
public final class CustosRequestDtos {

    private CustosRequestDtos() {
    }

    /** O centro de custo de um setor; sem nome, o do setor; sem tipo, o do tipo do setor. */
    public record NovoCentro(@NotNull(message = "Informe o setor.") UUID setorId,
                             @Size(max = 150, message = "O nome vai até 150 caracteres.") String nome,
                             TipoCentroCusto tipo) {
    }

    /** O nome, o tipo e se está ativo; o geral da unidade não se desativa. */
    public record AtualizacaoCentro(@NotBlank(message = "Informe o nome.") @Size(max = 150, message = "O nome vai até 150 caracteres.") String nome,
                                    @NotNull(message = "Informe o tipo.") TipoCentroCusto tipo,
                                    @NotNull(message = "Informe se está ativo.") Boolean ativo) {
    }

    public record Apurar(@NotBlank(message = "Informe a competência.")
                         @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "A competência é AAAA-MM.") String competencia) {
    }
}
