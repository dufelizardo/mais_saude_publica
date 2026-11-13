package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoInspecao;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoInspecao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Inspeção sanitária (ADR-0199): os fiscais pela matrícula (com login, o logado entre eles), o roteiro respondido, o resultado, o relatório, as irregularidades e, no retorno, as corrigidas. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InspecaoSanitariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o estabelecimento.")
    private UUID estabelecimentoId;
    @NotNull(message = "Informe o motivo.")
    private MotivoInspecao motivo;
    private LocalDate dataInspecao;
    private UUID denunciaId;
    private UUID surtoId;
    private UUID inspecaoOrigemId;
    private UUID roteiroVersaoId;
    private Map<String, RespostaRoteiro> respostas;
    @NotNull(message = "Informe o resultado.")
    private ResultadoInspecao resultado;
    @NotBlank(message = "Escreva o relatório da inspeção.")
    @Size(max = 4000, message = "O relatório vai até 4000 caracteres.")
    private String relatorio;
    @NotEmpty(message = "Informe ao menos um fiscal.")
    private List<@NotBlank(message = "Informe a matrícula do fiscal.") String> fiscais;
    @Valid
    private List<Irregularidade> irregularidades;
    /** No retorno: as irregularidades abertas do estabelecimento que foram corrigidas. */
    private List<UUID> corrigidas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Irregularidade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @Size(max = 40, message = "O código do item vai até 40 caracteres.")
        private String itemCodigo;
        @NotBlank(message = "Descreva a irregularidade.")
        @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
        private String descricao;
        @Size(max = 500, message = "O enquadramento vai até 500 caracteres.")
        private String enquadramento;
        @NotNull(message = "Informe a gravidade.")
        private GravidadeIrregularidade gravidade;
        private LocalDate prazoCorrecao;
    }
}
