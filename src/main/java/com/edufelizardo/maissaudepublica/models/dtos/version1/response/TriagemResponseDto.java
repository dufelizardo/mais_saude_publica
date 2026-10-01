package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import java.time.Instant;
import com.edufelizardo.maissaudepublica.models.Triagem;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoRisco;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TriagemResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID atendimentoUuid;
    private String profissionalMatricula;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private String pressaoArterial;
    private Double temperatura;
    private Double saturacaoOxigenio;
    private Integer frequenciaCardiaca;
    private Double peso;
    private ClassificacaoRisco classificacaoRisco;
    private String observacoes;

    // Retificação (ADR-0062)
    private UUID retificacaoDeUuid;
    private String motivoRetificacao;
    private Instant registradoEm;
    private String registradoPorCpf;
    /** Verdadeiro quando outra versão corrige este registro — ele deixa de ser o vigente. */
    private boolean retificado;
    private UUID retificadoPorUuid;

    public static TriagemResponseDto fromTriagem(Triagem triagem) {
        return fromTriagem(triagem, null);
    }

    /** {@code retificadoPor}: id da versão que corrige este registro, ou nulo se ele é o vigente (ADR-0062). */
    public static TriagemResponseDto fromTriagem(Triagem triagem, UUID retificadoPor) {
        return new TriagemResponseDto(
                triagem.getUuid(),
                triagem.getAtendimento().getUuid(),
                triagem.getProfissional().getMatricula(),
                triagem.getProfissional().getNome(),
                triagem.getDataHora(),
                triagem.getPressaoArterial(),
                triagem.getTemperatura(),
                triagem.getSaturacaoOxigenio(),
                triagem.getFrequenciaCardiaca(),
                triagem.getPeso(),
                triagem.getClassificacaoRisco(),
                triagem.getObservacoes(),
                triagem.getRetificacaoDe() != null ? triagem.getRetificacaoDe().getUuid() : null,
                triagem.getMotivoRetificacao(),
                triagem.getRegistradoEm(),
                triagem.getRegistradoPorCpf(),
                retificadoPor != null,
                retificadoPor
        );
    }
}
