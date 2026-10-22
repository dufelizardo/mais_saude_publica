package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Item do catálogo geral de procedimentos (ADR-0136, desenho na ADR-0111): serve a todas as especialidades, com o código
 * SIGTAP opcional para a integração futura. No procedimento odontológico, o alcance (dente ou face), a condição que ele
 * grava no odontograma quando realizado e se é só do cirurgião-dentista (Lei 11.889/2008).
 */
@Entity
@Table(name = "TB_PROCEDIMENTO_CATALOGO", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcedimentoCatalogo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    /** Nula: de uso geral, em qualquer atendimento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id", referencedColumnName = "uuid")
    private Especialidade especialidade;

    /** Código SIGTAP (10 dígitos), opcional. */
    @Column(length = 10)
    private String sigtap;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcanceProcedimento alcance;

    /** A condição gravada no odontograma quando o procedimento é realizado (restauração → restaurado). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condicao_resultante_id", referencedColumnName = "uuid")
    private CondicaoOdontologica condicaoResultante;

    /** Só o cirurgião-dentista registra (ODONTOLOGIA.REGISTRAR); sem a marca, o técnico em saúde bucal também. */
    private boolean exigeDentista;

    private boolean ativo;

    /** O procedimento é odontológico quando a especialidade é. */
    public boolean odontologico() {
        return especialidade != null && especialidade.isOdontologica();
    }
}
