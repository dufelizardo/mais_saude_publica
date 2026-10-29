package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaTipoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
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
}
