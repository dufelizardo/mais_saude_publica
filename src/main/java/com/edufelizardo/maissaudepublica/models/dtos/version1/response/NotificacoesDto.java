package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** As notificações do escopo (ADR-0124), com os números da tela. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificacoesDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long abertas;
    private long emInvestigacao;
    private long encerramentoAtrasado;
    private List<NotificacaoDto> lista;
    /** Contatos em acompanhamento com o monitoramento vencido, nas notificações visíveis (ADR-0130). */
    private long contatosVencidos;
}
