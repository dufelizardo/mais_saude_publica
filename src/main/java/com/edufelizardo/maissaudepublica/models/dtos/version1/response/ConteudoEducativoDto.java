package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Conteúdo educativo (ADR-0168). A situação já vem como expirado quando o publicado venceu. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConteudoEducativoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID grupoId;
    private int versao;
    private String titulo;
    private String resumo;
    private String texto;
    private String tema;
    private PublicoComunicacao publico;
    private String faixaEtaria;
    private LocalDate validade;
    private SituacaoConteudo situacao;
    private String motivoDevolucao;
    private LocalDateTime publicadoEm;
    private List<PublicacaoDto> publicacoes;
}
