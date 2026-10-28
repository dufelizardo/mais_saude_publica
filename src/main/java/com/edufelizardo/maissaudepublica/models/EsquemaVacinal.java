package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Uma dose do calendário vacinal (ADR-0147), como dado: o imunobiológico e a dose, o público, as idades mínima,
 * recomendada e máxima (em meses), o intervalo mínimo desde a dose anterior do mesmo imunobiológico, a repetição (o dT a
 * cada 10 anos) e a vigência. Quando o calendário nacional muda, cadastra-se a nova versão — não se programa. A dose
 * que mede a cobertura da vacina tem a meta (ADR-0149).
 */
@Entity
@Table(name = "TB_ESQUEMA_VACINAL", indexes = @Index(name = "ix_esquema_vacinal_imunobiologico", columnList = "imunobiologico_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EsquemaVacinal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imunobiologico_id", referencedColumnName = "uuid", nullable = false)
    private Imunobiologico imunobiologico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DoseVacinal dose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicoVacinal publico;

    private Integer idadeMinimaMeses;

    private Integer idadeRecomendadaMeses;

    private Integer idadeMaximaMeses;

    /** Desde a dose anterior do mesmo imunobiológico. */
    private Integer intervaloMinimoDias;

    /** Dose que se repete (o dT a cada 10 anos). */
    private Integer repeticaoAnos;

    /** Entra nas doses atrasadas e próximas do paciente; as demais são recomendações conforme a situação vacinal. */
    @Column(nullable = false)
    private boolean contaNaSituacao;

    @Column(length = 300)
    private String observacao;

    /** A dose de referência da cobertura da vacina (a 3ª da penta, a 1ª da tríplice viral), na criança (ADR-0149). */
    @Column(nullable = false)
    private boolean medeCobertura;

    /** A meta da cobertura, em % (90 para BCG e rotavírus, 95 para as demais, pelo PNI). */
    @Column(precision = 5, scale = 2)
    private BigDecimal metaCobertura;

    @Column(nullable = false)
    private LocalDate vigenteDesde;

    private LocalDate vigenteAte;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private boolean ativo;

    public boolean vigenteEm(LocalDate dia) {
        return ativo && !vigenteDesde.isAfter(dia) && (vigenteAte == null || !vigenteAte.isBefore(dia));
    }
}
