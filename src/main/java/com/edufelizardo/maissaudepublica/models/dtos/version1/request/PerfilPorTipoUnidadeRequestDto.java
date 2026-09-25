package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
public class PerfilPorTipoUnidadeRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private TipoUnidadeDeSaude tipo;

    @NotNull
    private UUID perfilAdministrativoId;
}
