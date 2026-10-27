package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** A situação agora (ADR-0144): quem está em atendimento e esperando, a maior espera e os alertas. Só números. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SituacaoAgoraDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime atualizadoEm;
    private UUID unidadeId;
    private String abrangencia;
    private long emAtendimento;
    private long aguardandoTriagem;
    private long aguardandoConsulta;
    /** Em minutos; nula quando ninguém aguarda. */
    private Long maiorEsperaMinutos;
    private List<AlertaSituacaoDto> alertas;
}
