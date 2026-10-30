package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEstruturaTemporaria;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Estrutura temporária de atendimento (ADR-0160). A localização vai com a latitude e a longitude juntas (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstruturaTemporariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo da estrutura.")
    private TipoEstruturaTemporaria tipo;
    @NotBlank(message = "Informe o nome da estrutura.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;
    @NotBlank(message = "Informe o local da estrutura.")
    @Size(max = 300, message = "O local vai até 300 caracteres.")
    private String local;
    @DecimalMin(value = "-90", message = "A latitude vai de -90 a 90.")
    @DecimalMax(value = "90", message = "A latitude vai de -90 a 90.")
    private Double latitude;
    @DecimalMin(value = "-180", message = "A longitude vai de -180 a 180.")
    @DecimalMax(value = "180", message = "A longitude vai de -180 a 180.")
    private Double longitude;
    @NotNull(message = "Informe a unidade de referência.")
    private UUID unidadeReferenciaId;
    @Min(value = 0, message = "Os leitos não ficam abaixo de zero.")
    private Integer leitos;
    @Min(value = 1, message = "Os atendimentos por dia são pelo menos 1.")
    private Integer atendimentosDia;
    @Min(value = 0, message = "As ambulâncias não ficam abaixo de zero.")
    private Integer ambulancias;
    @Size(max = 200, message = "O horário vai até 200 caracteres.")
    private String horario;
}
