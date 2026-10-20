package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeNotificacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampoFichaDto;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Um agravo do catálogo, com a ficha vigente (ADR-0124). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AgravoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private List<String> cids;
    private boolean compulsorio;
    private PeriodicidadeNotificacao periodicidade;
    private int prazoEncerramentoDias;
    private List<CriterioClassificacao> criterios;
    private boolean sensivel;
    private boolean ativo;
    private Integer fichaVersao;
    private List<CampoFichaDto> ficha;
}
