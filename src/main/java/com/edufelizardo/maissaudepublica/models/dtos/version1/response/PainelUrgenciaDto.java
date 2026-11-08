package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Painel da central de regulação das urgências (ADR-0176): as ocorrências abertas, aguardando regulação primeiro e depois
 * por gravidade e tempo de espera, e os números do dia.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PainelUrgenciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<OcorrenciaUrgenciaDto> abertas;
    private long aguardandoRegulacao;
    private long emAtendimento;
    /** Abertas por gravidade (VERMELHO, AMARELO, VERDE, AZUL). */
    private Map<String, Long> porPrioridade;
    private long recebidasHoje;
    private long concluidasHoje;
    /** Do chamado à regulação, nas reguladas hoje. */
    private Double tempoMedioAteRegulacaoMinutos;
    /** Do chamado ao envio do recurso, nos envios de hoje. */
    private Double tempoMedioAteEnvioMinutos;
    /** Viaturas do SAMU disponíveis, com plantão vigente e sem ocorrência aberta (ADR-0178). */
    private List<TransporteDtos.ViaturaDisponivel> viaturas;
}
