package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.ResponsavelPagamentoLicenca;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLicenca;
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
public class LicencaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID afastamentoId;

    @NotNull
    private TipoLicenca tipoLegal;

    @NotNull
    private ResponsavelPagamentoLicenca responsavelPagamento;

    private String documentoUrl;
}
