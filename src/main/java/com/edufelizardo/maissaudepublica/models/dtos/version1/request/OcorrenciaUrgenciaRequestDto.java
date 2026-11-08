package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.LocalRealizacao;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemOcorrenciaUrgencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Abertura da ocorrência pelo atendente da central (ADR-0176). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcorrenciaUrgenciaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a central de regulação.")
    private java.util.UUID centralId;
    @NotNull(message = "Informe a origem.")
    private OrigemOcorrenciaUrgencia origem;
    @Size(max = 100, message = "O nome de quem chamou vai até 100 caracteres.")
    private String solicitanteNome;
    @Size(max = 20, message = "O telefone vai até 20 caracteres.")
    private String solicitanteTelefone;
    /** Na origem UNIDADE_DE_SAUDE. */
    private java.util.UUID unidadeSolicitanteId;
    private java.util.UUID pacienteId;
    @Size(max = 200, message = "A descrição do paciente vai até 200 caracteres.")
    private String descricaoPaciente;
    private LocalRealizacao localTipo;
    @Size(max = 300, message = "O endereço vai até 300 caracteres.")
    private String endereco;
    @Size(max = 300, message = "A referência vai até 300 caracteres.")
    private String referencia;
    private Double latitude;
    private Double longitude;
    @NotBlank(message = "Descreva a queixa.")
    @Size(max = 2000, message = "A queixa vai até 2000 caracteres.")
    private String queixa;
    /** O atendente (TARM); sem matrícula, só o CPF do login fica registrado. */
    private String atendenteMatricula;
}
