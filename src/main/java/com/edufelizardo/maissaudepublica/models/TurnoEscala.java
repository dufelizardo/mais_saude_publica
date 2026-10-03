package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoTurno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Um turno da escala (ADR-0105): numa unidade, com data e hora de início e de fim (o fim pode cair no dia seguinte). Sem
 * profissional, é uma vaga aberta à espera de designação, com a função que falta.
 */
@Entity
@Table(name = "TB_TURNO_ESCALA", indexes = {
        @Index(name = "ix_turno_escala_unidade", columnList = "unidade_id, inicioEm"),
        @Index(name = "ix_turno_escala_profissional", columnList = "profissional_id, inicioEm")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TurnoEscala implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", referencedColumnName = "uuid")
    private Equipe equipe;

    /** Nulo: vaga aberta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid")
    private Profissional profissional;

    @Enumerated(EnumType.STRING)
    private FuncaoEquipe funcao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTurno tipo;

    @Column(nullable = false)
    private LocalDateTime inicioEm;

    @Column(nullable = false)
    private LocalDateTime fimEm;

    /** Atividade ou local, como no protótipo: "Imunização", "Sala de PA". */
    @Column(length = 120)
    private String descricao;

    private String registradoPorCpf;

    public boolean vaga() {
        return profissional == null;
    }

    public long minutos() {
        return Duration.between(inicioEm, fimEm).toMinutes();
    }
}
