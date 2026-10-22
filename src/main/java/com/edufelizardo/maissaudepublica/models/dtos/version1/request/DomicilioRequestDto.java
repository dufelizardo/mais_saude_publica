package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.AbastecimentoAgua;
import com.edufelizardo.maissaudepublica.models.enuns.DestinoLixo;
import com.edufelizardo.maissaudepublica.models.enuns.EscoamentoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.LocalizacaoDomicilio;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMoradia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Domicílio de um imóvel de moradia, com as condições (ADR-0127). O imóvel não muda na edição. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DomicilioRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o imóvel.")
    private UUID imovelId;

    @Size(max = 60, message = "A identificação vai até 60 caracteres.")
    private String identificacao;

    private SituacaoMoradia situacao;

    private LocalizacaoDomicilio localizacao;

    @Min(value = 1, message = "O domicílio tem 1 cômodo ou mais.")
    @Max(value = 50, message = "Até 50 cômodos.")
    private Integer comodos;

    private AbastecimentoAgua abastecimentoAgua;

    private EscoamentoSanitario escoamentoSanitario;

    private DestinoLixo destinoLixo;

    private Boolean energiaEletrica;

    /** Sem valor, ativo. */
    private Boolean ativo;
}
