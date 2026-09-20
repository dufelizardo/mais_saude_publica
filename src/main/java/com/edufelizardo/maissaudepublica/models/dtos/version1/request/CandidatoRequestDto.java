package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.StatusCandidato;
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
public class CandidatoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID vagaId;

    @NotBlank
    private String nome;

    @NotBlank
    private String cpf;

    private String curriculoUrl;

    @NotNull
    private StatusCandidato status;
}
