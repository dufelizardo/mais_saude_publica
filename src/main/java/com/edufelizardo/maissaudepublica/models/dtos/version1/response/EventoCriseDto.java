package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCriseSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma crise, com as etapas no detalhe (ADR-0123). Sem conteúdo liberado, só data, unidade e situação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoCriseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID acompanhamentoId;
    private Instant identificadaEm;
    private SituacaoCriseSaudeMental situacao;
    private boolean conteudoLiberado;
    private TipoCriseSaudeMental tipo;
    private String localOcorrencia;
    private String descricao;
    private DesfechoCriseSaudeMental desfecho;
    private Instant encerradaEm;
    private boolean notificacaoSugerida;
    /** Sugerida e ainda sem o registro de que foi feita. */
    private boolean notificacaoPendente;
    private LocalDate notificadaEm;
    private String notificacaoNumero;
    private String profissionalNome;
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
        private EtapaCriseSaudeMental etapa;
        private String descricao;
        private Instant ocorridaEm;
        private String profissionalNome;
    }
}
