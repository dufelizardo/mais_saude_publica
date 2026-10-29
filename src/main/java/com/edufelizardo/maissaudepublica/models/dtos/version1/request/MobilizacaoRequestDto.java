package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoMobilizacao;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Mobilização (ADR-0158): o profissional pela matrícula; os demais tipos pelo recurso. Sem início, hoje. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MobilizacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo.")
    private TipoMobilizacao tipo;
    private UUID origemId;
    @NotNull(message = "Informe a unidade de destino.")
    private UUID destinoId;
    private String profissionalMatricula;
    @Size(max = 300, message = "O recurso vai até 300 caracteres.")
    private String recurso;
    @Positive(message = "A quantidade é acima de zero.")
    private Integer quantidade;
    @Size(max = 200, message = "O responsável vai até 200 caracteres.")
    private String responsavel;
    private LocalDate inicio;
    private LocalDate fim;
    private UUID capacidadeAtivacaoId;
}
