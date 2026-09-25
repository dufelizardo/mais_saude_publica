package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Getter
@Setter
public class SetorRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID unidadeId;

    @NotBlank
    private String nome;

    @NotBlank
    private String codigo;

    @NotNull
    private TipoSetor tipo;

    @NotNull
    private Boolean ativo;

    /**
     * Opcional — referencia um {@link com.edufelizardo.maissaudepublica.models.Profissional} do RH
     * pela matrícula (mesma convenção de {@code LotacaoRequestDto.matriculaProfissional}, FK direta
     * por trás, ver ADR-0034). {@code null}/em branco desvincula o responsável atual.
     */
    private String matriculaResponsavel;
}
