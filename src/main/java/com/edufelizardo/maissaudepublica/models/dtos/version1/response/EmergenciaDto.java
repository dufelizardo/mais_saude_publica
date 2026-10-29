package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaTipoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeAcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.RecursoCapacidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoCapacidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroAtivacao;
import com.edufelizardo.maissaudepublica.models.enuns.NivelImpacto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDecretoEmergencia;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A emergência (ADR-0156). Na lista, o território, o histórico e as avaliações vão vazios. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EmergenciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private UUID eventoId;
    private UUID tipoId;
    private String tipoNome;
    private CategoriaTipoEmergencia categoria;
    private String cobrade;
    private UUID nivelId;
    private int nivelOrdem;
    private String nivelNome;
    private SituacaoEmergencia situacao;
    private Instant inicio;
    private Instant fim;
    private String motivoEncerramento;
    private String responsavelMatricula;
    private String responsavelNome;
    private NivelImpacto ultimoImpacto;
    private Instant ultimaAvaliacaoEm;
    private String geometria;
    private List<MicroareaAfetada> microareas;
    private String espinNumero;
    private LocalDate espinData;
    private TipoDecretoEmergencia decretoTipo;
    private String decretoNumero;
    private LocalDate decretoData;
    private List<MudancaNivel> niveis;
    private List<Avaliacao> avaliacoes;
    /** Os planos de contingência ativados na emergência (ADR-0157), os ativos primeiro; na lista, vazio. */
    private List<PlanoAtivo> planos;
    /** A coordenação (ADR-0158): ações, quantas atrasadas (também na lista), mobilizações, capacidade ativada e unidades afetadas. */
    private List<AcaoItem> acoes;
    private int acoesAtrasadas;
    private List<MobilizacaoItem> mobilizacoes;
    private List<CapacidadeAtivada> capacidades;
    private List<UnidadeAfetada> unidadesAfetadas;
    /** A desmobilização (ADR-0159), quando iniciada, e o que falta para encerrar. */
    private DesmobilizacaoItem desmobilizacao;
    private List<String> pendencias;

    /** Microárea do território afetado. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class MicroareaAfetada implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String codigo;
        private String nome;
        private String areaNome;
        private String geometria;
    }

    /** Uma mudança de nível; sem nível anterior, a ativação. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class MudancaNivel implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nivelAnterior;
        private String nivelNovo;
        private int nivelNovoOrdem;
        private String justificativa;
        private Instant mudadoEm;
        private String mudadoPorCpf;
    }

    /** Uma avaliação de impacto. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Avaliacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private Instant avaliadaEm;
        private Integer populacaoAfetada;
        private List<String> unidadesAfetadas;
        private Integer profissionaisIndisponiveis;
        private Integer leitosIndisponiveis;
        private String medicamentosCriticos;
        private String equipamentos;
        private String transporte;
        private String infraestrutura;
        private NivelImpacto nivelImpacto;
        private String observacao;
    }

    /** Um plano ativado na emergência, com os passos e as ações previstas para o nível atual. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class PlanoAtivo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID ativacaoId;
        private UUID planoId;
        private String codigo;
        private String nome;
        private int versao;
        private SituacaoAtivacaoPlano situacao;
        private Instant ativadaEm;
        private Instant desativadaEm;
        /** O plano cobre o nível atual da emergência. */
        private boolean cobreNivelAtual;
        private List<RegistroAtivacao> registros;
        private List<AcaoPrevista> acoesDoNivel;
    }

    /** Um passo da ativação. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class RegistroAtivacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private TipoRegistroAtivacao tipo;
        private String nivelNome;
        private boolean nivelCoberto;
        private String autoridade;
        private String justificativa;
        private Instant registradoEm;
    }

    /** Uma ação prevista no plano para o nível atual, com o prazo calculado. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class AcaoPrevista implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String descricao;
        private String responsavel;
        private Integer prazoHoras;
        private Instant prazoEm;
        private String unidadeNome;
    }

    /** Uma ação emergencial do quadro. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class AcaoItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String descricao;
        private String responsavel;
        private String unidadeNome;
        private PrioridadeAcaoEmergencial prioridade;
        private Instant prazo;
        private SituacaoAcaoEmergencial situacao;
        private boolean atrasada;
        private String planoCodigo;
        private Instant iniciadaEm;
        private Instant concluidaEm;
        private String motivoCancelamento;
        private String observacao;
    }

    /** Uma mobilização; a situação da ligada a uma transferência acompanha a dela. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class MobilizacaoItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private TipoMobilizacao tipo;
        private String origemNome;
        private UUID destinoId;
        private String destinoNome;
        private String profissionalMatricula;
        private String profissionalNome;
        private String recurso;
        private Integer quantidade;
        private String responsavel;
        private LocalDate inicio;
        private LocalDate fim;
        private SituacaoMobilizacao situacao;
        private UUID transferenciaId;
        private StatusTransferenciaFarmacia transferenciaStatus;
        private boolean autorizaEscala;
        private String motivoCancelamento;
    }

    /** Capacidade emergencial ativada na emergência. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class CapacidadeAtivada implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID capacidadeId;
        private String unidadeNome;
        private RecursoCapacidade recurso;
        private int normal;
        private int emergencial;
        private int quantidade;
        private SituacaoAtivacaoCapacidade situacao;
        private Instant inicio;
        private Instant fim;
        private String justificativa;
        private String justificativaFim;
    }

    /** Unidade da última avaliação de impacto, com a situação operacional atual. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class UnidadeAfetada implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
        private SituacaoOperacional situacaoOperacional;
    }

    /** O registro da desmobilização: quem, por quê e o que foi liberado. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class DesmobilizacaoItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Instant iniciadaEm;
        private String responsavel;
        private String justificativa;
        private int mobilizacoesConcluidas;
        private int profissionaisLiberados;
        private int capacidadesDesativadas;
        private int planosDesativados;
    }
}
