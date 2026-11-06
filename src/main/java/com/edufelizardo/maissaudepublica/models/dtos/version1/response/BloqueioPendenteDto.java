package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Caso notificado de arbovirose sem ação de bloqueio (ADR-0162): só o lugar e as datas, sem o paciente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BloqueioPendenteDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID notificacaoId;
    private String agravoNome;
    private LocalDate dataNotificacao;
    private LocalDate dataPrimeirosSintomas;
    private String unidadeNome;
    private UUID microareaId;
    private String microareaCodigo;
    private String areaNome;
    private String localProvavel;
    private Double latitude;
    private Double longitude;
}
