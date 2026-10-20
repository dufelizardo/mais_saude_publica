package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampoFichaDto;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Uma notificação (ADR-0124). Em agravo sensível, sem o conteúdo para quem não investiga nem notificou
 * (essas nem aparecem na lista). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotificacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID agravoId;
    private String agravoCodigo;
    private String agravoNome;
    private boolean sensivel;
    private PeriodicidadeNotificacao periodicidade;
    private UUID unidadeId;
    private String unidadeNome;
    private String profissionalNome;
    private LocalDate dataPrimeirosSintomas;
    private LocalDate dataNotificacao;
    private OrigemNotificacao origem;
    private UUID criseId;
    private String localProvavel;
    private Map<String, Object> respostas;
    private Integer fichaVersao;
    private List<CampoFichaDto> ficha;
    private SituacaoNotificacao situacao;
    private LocalDate prazoEncerramento;
    private boolean encerramentoAtrasado;
    private ClassificacaoCasoEpidemiologico classificacaoAtual;
    private ClassificacaoCasoEpidemiologico classificacaoFinal;
    private EvolucaoCaso evolucao;
    private Instant encerradaEm;
    /** Outra notificação do mesmo agravo em aberto para o paciente (aviso, não bloqueio). */
    private String aviso;
    private Investigacao investigacao;
    private List<Classificacao> classificacoes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Investigacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String investigadorNome;
        private Instant iniciadaEm;
        private String exposicao;
        private String localProvavelInfeccao;
        private String medidas;
        private List<UUID> examesIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Classificacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private ClassificacaoCasoEpidemiologico classificacao;
        private CriterioClassificacao criterio;
        private String justificativa;
        private Instant classificadaEm;
        private String responsavelNome;
    }
}
