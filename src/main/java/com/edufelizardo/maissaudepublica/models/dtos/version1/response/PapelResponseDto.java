package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.Permissao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PapelResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private boolean ativo;
    /** Papel semeado pelo catálogo (ADR-0066); os demais foram criados pela administração. */
    private boolean padrao;
    private List<String> permissoes;

    public static PapelResponseDto fromPapel(Papel p, boolean padrao) {
        return new PapelResponseDto(p.getUuid(), p.getCodigo(), p.getNome(), p.getDescricao(), p.isAtivo(), padrao,
                p.getPermissoes().stream().map(Permissao::getCodigo).sorted().toList());
    }
}
