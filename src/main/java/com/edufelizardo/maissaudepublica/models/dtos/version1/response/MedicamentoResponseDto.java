package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Medicamento;
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
public class MedicamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private String principioAtivo;
    private String apresentacao;
    private String codigo;
    private boolean ativo;

    public static MedicamentoResponseDto fromMedicamento(Medicamento medicamento) {
        return new MedicamentoResponseDto(
                medicamento.getUuid(),
                medicamento.getNome(),
                medicamento.getPrincipioAtivo(),
                medicamento.getApresentacao(),
                medicamento.getCodigo(),
                medicamento.isAtivo()
        );
    }
}
