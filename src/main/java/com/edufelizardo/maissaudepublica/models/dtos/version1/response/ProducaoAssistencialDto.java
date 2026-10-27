package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Produção assistencial no período, na unidade ou nas unidades do acesso (ADR-0143). Só agregados: nunca o paciente.
 * O período anterior tem a mesma duração e termina na véspera do início.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProducaoAssistencialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate de;
    private LocalDate ate;
    private LocalDate anteriorDe;
    private LocalDate anteriorAte;
    /** A unidade pedida; nula quando é a rede (ou as unidades do acesso). */
    private UUID unidadeId;
    private String unidadeNome;
    /** "Rede municipal", "Unidades do seu acesso" ou o nome da unidade. */
    private String abrangencia;
    private long atendimentos;
    private long atendimentosAnterior;
    /** Variação sobre o período anterior, em %; nula quando o anterior é zero. */
    private BigDecimal variacao;
    private BigDecimal mediaDiaria;
    private List<ContagemProducaoDto> porTipo;
    private List<ContagemProducaoDto> porEspecialidade;
    private List<ContagemProducaoDto> porCategoria;
    private List<ContagemProducaoDto> porFaixaEtaria;
    private List<ContagemProducaoDto> porSexo;
    /** 24 posições: atendimentos concluídos em cada hora do dia. */
    private List<Long> porHora;
    private AgendaProducaoDto agenda;
    private AgendaProducaoDto agendaAnterior;
}
