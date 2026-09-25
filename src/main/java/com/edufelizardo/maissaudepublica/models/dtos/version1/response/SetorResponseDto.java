package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SetorResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeUuid;
    private String unidadeNome;
    private String nome;
    private String codigo;
    private TipoSetor tipo;
    private boolean ativo;
    private String responsavelMatricula;
    private String responsavelNome;

    public static SetorResponseDto fromSetor(Setor setor) {
        String responsavelMatricula = null;
        String responsavelNome = null;
        if (setor.getResponsavel() != null) {
            responsavelMatricula = setor.getResponsavel().getMatricula();
            responsavelNome = setor.getResponsavel().getNome();
        }
        return new SetorResponseDto(
                setor.getUuid(),
                setor.getUnidade().getUuid(),
                setor.getUnidade().getNome(),
                setor.getNome(),
                setor.getCodigo(),
                setor.getTipo(),
                setor.isAtivo(),
                responsavelMatricula,
                responsavelNome
        );
    }
}
