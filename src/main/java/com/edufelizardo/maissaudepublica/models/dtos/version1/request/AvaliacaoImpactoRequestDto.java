package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.NivelImpacto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Avaliação de impacto (ADR-0156): o nível e o que se sabe de cada dimensão. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AvaliacaoImpactoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @PositiveOrZero(message = "A população não pode ser negativa.")
    private Integer populacaoAfetada;
    private List<UUID> unidadeIds;
    @PositiveOrZero(message = "O número não pode ser negativo.")
    private Integer profissionaisIndisponiveis;
    @PositiveOrZero(message = "O número não pode ser negativo.")
    private Integer leitosIndisponiveis;
    @Size(max = 1000)
    private String medicamentosCriticos;
    @Size(max = 1000)
    private String equipamentos;
    @Size(max = 1000)
    private String transporte;
    @Size(max = 1000)
    private String infraestrutura;
    @NotNull(message = "Informe o nível de impacto.")
    private NivelImpacto nivelImpacto;
    @Size(max = 2000)
    private String observacao;
}
