package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.TipoIncidente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Notificação de incidente de segurança do paciente (ADR-0203): anônima ou identificada; o paciente é opcional. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class IncidenteSegurancaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a unidade.")
    private UUID unidadeId;
    private UUID pacienteId;
    @NotNull(message = "Informe quando ocorreu.")
    private Instant ocorridoEm;
    @NotBlank(message = "Informe o local.")
    @Size(max = 200, message = "O local vai até 200 caracteres.")
    private String local;
    @NotNull(message = "Informe o tipo do incidente.")
    private TipoIncidente tipo;
    private GrauDano danoPercebido;
    @NotBlank(message = "Descreva o que aconteceu.")
    @Size(max = 4000, message = "A descrição vai até 4000 caracteres.")
    private String descricao;
    @Size(max = 2000, message = "A ação imediata vai até 2000 caracteres.")
    private String acaoImediata;
    private Boolean anonima;
}
