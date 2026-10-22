package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** As notificações negativas e as semanas sem notificação nem declaração, por unidade (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SemanasEpidemiologicasDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int anoAtual;
    private int semanaAtual;
    private List<NotificacaoNegativaDto> declaradas;
    /** Semanas recentes (as últimas 8 encerradas) sem notificação nem declaração, por unidade do escopo. */
    private List<NotificacaoNegativaDto> pendentes;
}
