package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Candidato;
import com.edufelizardo.maissaudepublica.models.enuns.StatusCandidato;
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
public class CandidatoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID vagaId;
    private String nome;
    private String cpf;
    private String curriculoUrl;
    private StatusCandidato status;

    public static CandidatoResponseDto fromCandidato(Candidato candidato) {
        return new CandidatoResponseDto(
                candidato.getUuid(),
                candidato.getVaga().getUuid(),
                candidato.getNome(),
                candidato.getCpf(),
                candidato.getCurriculoUrl(),
                candidato.getStatus()
        );
    }
}
