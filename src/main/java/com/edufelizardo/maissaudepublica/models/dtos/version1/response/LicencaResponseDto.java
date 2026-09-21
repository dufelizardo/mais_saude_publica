package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Licenca;
import com.edufelizardo.maissaudepublica.models.enuns.ResponsavelPagamentoLicenca;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLicenca;
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
public class LicencaResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID afastamentoId;
    private String profissionalMatricula;
    private String profissionalNome;
    private TipoLicenca tipoLegal;
    private ResponsavelPagamentoLicenca responsavelPagamento;
    private String documentoUrl;

    public static LicencaResponseDto fromLicenca(Licenca licenca) {
        return new LicencaResponseDto(
                licenca.getUuid(),
                licenca.getAfastamento().getUuid(),
                licenca.getAfastamento().getProfissional().getMatricula(),
                licenca.getAfastamento().getProfissional().getNome(),
                licenca.getTipoLegal(),
                licenca.getResponsavelPagamento(),
                licenca.getDocumentoUrl()
        );
    }
}
