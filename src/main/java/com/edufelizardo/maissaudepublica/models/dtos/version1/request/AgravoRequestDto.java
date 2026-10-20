package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeNotificacao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** Agravo do catálogo (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AgravoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código.")
    @Pattern(regexp = "^[A-Z0-9_]{2,60}$", message = "O código usa letras maiúsculas, números e sublinhado (ex.: DENGUE).")
    private String codigo;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;

    private List<String> cids;

    /** Sem valor, compulsório. */
    private Boolean compulsorio;

    @NotNull(message = "Informe a periodicidade.")
    private PeriodicidadeNotificacao periodicidade;

    @NotNull(message = "Informe o prazo de encerramento.")
    @Min(value = 1, message = "O prazo de encerramento é de 1 dia ou mais.")
    @Max(value = 730, message = "O prazo de encerramento vai até 730 dias.")
    private Integer prazoEncerramentoDias;

    @NotEmpty(message = "Informe pelo menos um critério de classificação.")
    private List<CriterioClassificacao> criterios;

    private Boolean sensivel;

    /** Dias de monitoramento do contato a partir da exposição (ADR-0130); sem valor, o agravo não define. */
    @Min(value = 1, message = "O monitoramento do contato é de 1 dia ou mais.")
    @Max(value = 365, message = "O monitoramento do contato vai até 365 dias.")
    private Integer periodoMonitoramentoContatoDias;

    /** Sem valor, ativo. */
    private Boolean ativo;
}
