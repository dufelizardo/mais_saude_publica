package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.ArcadaDentaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProtese;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Abertura do tratamento protético no atendimento de odontologia (ADR-0139). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TratamentoProteticoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o atendimento.")
    private UUID atendimentoId;

    @NotBlank(message = "Informe a matrícula do dentista responsável.")
    private String profissionalMatricula;

    @NotNull(message = "Informe o tipo: TOTAL, PARCIAL_REMOVIVEL ou UNITARIA.")
    private TipoProtese tipo;

    /** Na total e na parcial removível. */
    private ArcadaDentaria arcada;

    /** Na unitária (FDI). */
    private Integer dente;

    /** O laboratório da rede (unidade com a capacidade LRPD)... */
    private UUID laboratorioUnidadeId;

    /** ...ou o contratado, pelo nome. */
    @Size(max = 200, message = "O nome do laboratório vai até 200 caracteres.")
    private String laboratorioExterno;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
