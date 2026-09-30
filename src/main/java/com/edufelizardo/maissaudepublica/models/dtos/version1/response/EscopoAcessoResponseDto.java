package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Unidade em que um acesso pode ser concedido (ADR-0068), de qualquer nível da hierarquia — Federal a
 * UBS —, com a unidade acima para a tela mostrar a árvore.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EscopoAcessoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private TipoUnidadeDeSaude tipo;
    private UUID unidadeSuperiorUuid;

    public static EscopoAcessoResponseDto fromUnidade(UnidadeDeSaude u) {
        return new EscopoAcessoResponseDto(u.getUuid(), u.getNome(), u.getTipo(),
                u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getUuid() : null);
    }
}
