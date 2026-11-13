package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeIdade;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Faixa de referência de um exame numérico por sexo e idade (ADR-0180). Sem sexo, vale para os dois. A idade vai com a sua
 * unidade (dias, meses ou anos) e é comparada pelo calendário: "de 12 anos" vale a partir do 12º aniversário. O mínimo entra
 * na faixa e o máximo não; sem máximo, a faixa não tem limite. Os críticos são os valores de pânico. A faixa é copiada para
 * o resultado no registro: removê-la ou mudá-la não muda resultado já dado.
 */
@Entity
@Table(name = "TB_FAIXA_REFERENCIA_EXAME")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "exame")
@EqualsAndHashCode(of = "uuid")
public class FaixaReferenciaExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exame_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ExameLaboratorial exame;

    /** Nulo: vale para os dois sexos. Nunca {@code IGNORADO}. */
    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Column(nullable = false)
    private int idadeMinima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadeIdade unidadeIdadeMinima;

    /** Exclusiva; nula: sem limite. */
    private Integer idadeMaxima;

    @Enumerated(EnumType.STRING)
    private UnidadeIdade unidadeIdadeMaxima;

    @Column(precision = 14, scale = 4)
    private BigDecimal referenciaMinima;

    @Column(precision = 14, scale = 4)
    private BigDecimal referenciaMaxima;

    @Column(precision = 14, scale = 4)
    private BigDecimal criticoMinimo;

    @Column(precision = 14, scale = 4)
    private BigDecimal criticoMaximo;

    /** Rótulo curto opcional, por exemplo "Recém-nascido". */
    @Column(length = 100)
    private String descricao;

    /** A faixa cobre quem nasceu em {@code nascimento} na data {@code data}, pelo calendário. */
    public boolean cobre(LocalDate nascimento, LocalDate data) {
        if (data.isBefore(nascimento.plus(unidadeIdadeMinima.periodo(idadeMinima)))) {
            return false;
        }
        return idadeMaxima == null || data.isBefore(nascimento.plus(unidadeIdadeMaxima.periodo(idadeMaxima)));
    }

    /** Início da faixa em dias aproximados (para ordenar e comparar faixas). */
    public double inicioAproximado() {
        return unidadeIdadeMinima.diasAproximados(idadeMinima);
    }

    /** Fim da faixa em dias aproximados; sem máximo, infinito. */
    public double fimAproximado() {
        return idadeMaxima == null ? Double.POSITIVE_INFINITY : unidadeIdadeMaxima.diasAproximados(idadeMaxima);
    }

    /** As duas faixas têm idades em comum. */
    public boolean sobrepoe(FaixaReferenciaExame outra) {
        return inicioAproximado() < outra.fimAproximado() && outra.inicioAproximado() < fimAproximado();
    }
}
