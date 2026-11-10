package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Leito de internação ou de observação (ADR-0098), num setor assistencial da unidade. Não se apaga: sai de uso. */
@Entity
@Table(name = "TB_LEITO", uniqueConstraints = @UniqueConstraint(name = "uk_leito_identificacao_por_unidade",
        columnNames = {"unidade_id", "identificacao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Leito implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", nullable = false)
    private Setor setor;

    /** Como a equipe chama o leito, ex.: "Enf. 2 · Leito 03". Única na unidade. */
    @Column(nullable = false, length = 60)
    private String identificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLeito tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexoLeito sexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoLeito situacao;

    @Column(length = 500)
    private String motivoBloqueio;

    @Column(nullable = false)
    private boolean ativo;

    /** Quem o leito recebe pela idade (ADR-0184). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito faixaEtaria;

    /** Reserva da central de leitos (ADR-0184): até quando e para qual pedido. Vencida, o leito conta como livre. */
    private java.time.Instant reservadoAte;

    private java.util.UUID reservaSolicitacaoId;

    /** A faixa etária que o tipo do leito sugere (ADR-0184): a mesma regra da migração. */
    public static com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito faixaDoTipo(TipoLeito tipo) {
        if (tipo == null) {
            return com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito.ADULTO;
        }
        return switch (tipo) {
            case UTI_NEONATAL -> com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito.NEONATAL;
            case PEDIATRICO, UTI_PEDIATRICA -> com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito.PEDIATRICO;
            default -> com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito.ADULTO;
        };
    }

    /** Sem faixa informada, vale a do tipo. */
    @PrePersist
    void faixaPeloTipoSeFaltar() {
        if (faixaEtaria == null) {
            faixaEtaria = faixaDoTipo(tipo);
        }
    }

    /** A situação considerando a reserva vencida como livre. */
    public SituacaoLeito situacaoEm(java.time.Instant agora) {
        if (situacao == SituacaoLeito.RESERVADO && reservadoAte != null && reservadoAte.isBefore(agora)) {
            return SituacaoLeito.LIVRE;
        }
        return situacao;
    }
}
