package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaTipoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoPlano;
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
}
