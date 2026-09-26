package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

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

    public static TriagemResponseDto fromTriagem(Triagem triagem) {
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
                triagem.getObservacoes()
        );
    }
}
