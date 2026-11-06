package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.EspecieVetor;
import com.edufelizardo.maissaudepublica.models.enuns.FaseFoco;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoVistoriaVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDeposito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVistoriaVetorial;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Vistoria do agente de endemias (ADR-0162): sem a data, hoje; os focos só na inspecionada. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VistoriaVetorialRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o imóvel.")
    private UUID imovelId;
    private LocalDate data;
    @NotBlank(message = "Informe a matrícula do agente.")
    private String agenteMatricula;
    @NotNull(message = "Informe o tipo da vistoria.")
    private TipoVistoriaVetorial tipo;
    @NotNull(message = "Informe o resultado.")
    private ResultadoVistoriaVetorial resultado;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosA1;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosA2;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosB;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosC;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosD1;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosD2;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosE;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosEliminados;
    @Min(value = 0, message = "Os depósitos não ficam abaixo de zero.")
    private Integer depositosTratados;
    @Size(max = 100, message = "O larvicida vai até 100 caracteres.")
    private String larvicida;
    @DecimalMin(value = "0", message = "A quantidade não fica abaixo de zero.")
    private Double larvicidaGramas;
    private UUID acaoId;
    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
    @Valid
    private List<Foco> focos;

    /** Um depósito positivo. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Foco implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @NotNull(message = "Informe o tipo do depósito do foco.")
        private TipoDeposito tipoDeposito;
        @NotNull(message = "Informe a fase encontrada.")
        private FaseFoco fase;
        private EspecieVetor especieSuspeita;
        private Boolean amostraColetada;
    }
}
