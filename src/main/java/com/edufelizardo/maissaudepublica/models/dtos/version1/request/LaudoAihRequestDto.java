package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CausaExternaAih;
import com.edufelizardo.maissaudepublica.models.enuns.ClinicaAih;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Laudo para solicitação de AIH (ADR-0186): de uma internação ou, antes da admissão, de uma internação eletiva agendada. Na
 * correção, a internação e o agendamento não mudam.
 */
@Data
public class LaudoAihRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID internacaoId;

    private UUID agendamentoId;

    @NotBlank(message = "Informe a matrícula do médico solicitante.")
    private String profissionalMatricula;

    @NotBlank(message = "Informe os principais sinais e sintomas.")
    @Size(max = 2000, message = "Os sinais e sintomas vão até 2000 caracteres.")
    private String sinaisSintomas;

    @NotBlank(message = "Informe as condições que justificam a internação.")
    @Size(max = 2000, message = "As condições vão até 2000 caracteres.")
    private String condicoesJustificativas;

    @Size(max = 2000, message = "Os resultados de exames vão até 2000 caracteres.")
    private String resultadosExames;

    @NotBlank(message = "Informe o diagnóstico inicial.")
    @Size(max = 500, message = "O diagnóstico inicial vai até 500 caracteres.")
    private String diagnosticoInicial;

    @NotBlank(message = "Informe o CID-10 principal.")
    @Pattern(regexp = "^\\s*[A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?\\s*$", message = "CID-10 inválido (ex.: K80.2).")
    private String cidPrincipal;

    @Pattern(regexp = "^\\s*([A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?)?\\s*$", message = "CID-10 secundário inválido.")
    private String cidSecundario;

    @Pattern(regexp = "^\\s*([A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?)?\\s*$", message = "CID-10 de causa associada inválido.")
    private String cidCausaAssociada;

    @NotNull(message = "Informe o procedimento solicitado.")
    private UUID procedimentoId;

    @NotNull(message = "Informe a clínica.")
    private ClinicaAih clinica;

    private CausaExternaAih causaExterna;
}
