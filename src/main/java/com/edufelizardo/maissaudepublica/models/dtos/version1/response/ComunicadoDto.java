package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeComunicado;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoComunicado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Comunicado (ADR-0168), com o histórico de publicação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComunicadoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String titulo;
    private String mensagem;
    private PrioridadeComunicado prioridade;
    private PublicoComunicacao publico;
    private LocalDate inicio;
    private LocalDate fim;
    private UUID unidadeId;
    private String unidadeNome;
    private List<UUID> canalIds;
    private SituacaoComunicado situacao;
    /** Publicado e dentro do período. */
    private boolean vigente;
    private LocalDateTime publicadoEm;
    private LocalDate encerradoEm;
    private String motivoEncerramento;
    private List<PublicacaoDto> publicacoes;
    private UUID campanhaId;
    private String campanhaNome;
}
