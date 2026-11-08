package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoUrgencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Envio, chegada ao local, saída do local ou chegada ao destino, pelo rádio-operador (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DeslocamentoUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o passo do deslocamento.")
    private TipoEventoUrgencia tipo;
    /** Sem hora, agora. */
    private java.time.Instant ocorridoEm;
    @Size(max = 60, message = "A identificação do recurso vai até 60 caracteres.")
    private String recursoIdentificacao;
    @Size(max = 500, message = "A observação vai até 500 caracteres.")
    private String observacao;
}
