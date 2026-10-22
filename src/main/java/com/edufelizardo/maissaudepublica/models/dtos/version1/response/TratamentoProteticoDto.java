package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ArcadaDentaria;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaProtetica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTratamentoProtetico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProtese;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O tratamento protético com as etapas (ADR-0139). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TratamentoProteticoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private UUID atendimentoId;
    private UUID planoId;
    private String unidadeNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private TipoProtese tipo;
    private ArcadaDentaria arcada;
    private Integer dente;
    private UUID laboratorioUnidadeId;
    private String laboratorioNome;
    private boolean laboratorioContratado;
    private EtapaProtetica etapaAtual;
    private SituacaoTratamentoProtetico situacao;
    private LocalDate dataEntrega;
    private String observacao;
    private Instant abertoEm;
    private Instant encerradoEm;
    private String motivoCancelamento;
    /** As etapas que ainda podem ser registradas, na ordem. */
    private List<EtapaProtetica> proximasEtapas;
    private List<Etapa> etapas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Etapa implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private EtapaProtetica etapa;
        private LocalDate data;
        private String profissionalNome;
        private String observacao;
        private Instant registradoEm;
    }
}
