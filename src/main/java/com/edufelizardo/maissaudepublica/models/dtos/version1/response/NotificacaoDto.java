package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CausalidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.CondutaEsquemaEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.CriterioClassificacao;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemLocalCaso;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContato;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExposicaoContato;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampoFichaDto;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    /** Local do caso (ADR-0130). */
    private Double latitude;
    private Double longitude;
    private OrigemLocalCaso origemLocal;
    private String microareaCodigo;
    private String areaCodigo;
    private List<Contato> contatos;
    /** Surto, medidas e linha do tempo do caso (ADR-0131). */
    private UUID surtoId;
    private String surtoNome;
    private List<MedidaControleDto> medidas;
    private List<EventoLinhaDoTempoDto> linhaDoTempo;
    /** ESAVI e erro de imunização (ADR-0150). */
    private GravidadeEsavi gravidadeEsavi;
    private CausalidadeEsavi causalidadeEsavi;
    private CondutaEsquemaEsavi condutaEsavi;
    private List<Dose> doses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Dose implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID imunobiologicoId;
        private String sigla;
        private String doseRotulo;
        private LocalDateTime aplicadaEm;
        private String lote;
        private String unidadeNome;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Contato implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID pacienteId;
        private String nome;
        private String telefone;
        private String endereco;
        private TipoExposicaoContato tipoExposicao;
        private LocalDate dataExposicao;
        private LocalDate fimMonitoramento;
        private boolean monitoramentoVencido;
        private SituacaoContato situacao;
        private String observacao;
        private UUID notificacaoGeradaId;
    }

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
