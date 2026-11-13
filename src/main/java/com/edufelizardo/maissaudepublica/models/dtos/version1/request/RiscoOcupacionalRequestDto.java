package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoOcupacional;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** O risco ocupacional de um empregador (ADR-0208). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RiscoOcupacionalRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo do risco.")
    private TipoRiscoOcupacional tipo;

    @NotBlank(message = "Informe o agente (ex.: ruído, sílica, benzeno).")
    @Size(max = 200, message = "O agente vai até 200 caracteres.")
    private String agente;

    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;

    /** CPF ou CNPJ do empregador, conferido. */
    private String empregadorDocumento;

    @Size(max = 200, message = "O nome do empregador vai até 200 caracteres.")
    private String empregadorNome;

    @Size(max = 500, message = "O local vai até 500 caracteres.")
    private String local;

    @Min(value = 0, message = "Os trabalhadores expostos são 0 ou mais.")
    private Integer trabalhadoresExpostos;

    /** A notificação cuja investigação encontrou o risco; o empregador vem do bloco do trabalho, se não informado. */
    private UUID notificacaoId;
}
