package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AceitacaoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoChecagem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Checagem de um item da prescrição hospitalar (ADR-0187): de um horário aprazado ou, no "se necessário", do próprio item. Registro
 * clínico: não é editado; corrigir é retificar (ADR-0062), que estorna a baixa do lote da versão anterior.
 */
@Entity
@Table(name = "TB_CHECAGEM_PRESCRICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ChecagemPrescricao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ItemPrescricao item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id", referencedColumnName = "uuid", updatable = false)
    private HorarioPrescricao horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ResultadoChecagem resultado;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private AceitacaoDieta aceitacao;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private MotivoNaoAdministracao motivoNaoAdministracao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", referencedColumnName = "uuid", updatable = false)
    private Lote lote;

    @Column(updatable = false)
    private Integer quantidade;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant realizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private ChecagemPrescricao retificacaoDe;

    @Column(length = 1000, updatable = false)
    private String motivoRetificacao;

    /** Marcada quando uma retificação a substitui. */
    private boolean retificada;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
