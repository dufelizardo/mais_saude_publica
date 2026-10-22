package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Complemento odontológico do procedimento (ADR-0136): o dente (FDI) e as faces. O procedimento odontológico é o
 * procedimento normal mais este complemento; realizado, ele grava os registros do odontograma.
 */
@Entity
@Table(name = "TB_COMPLEMENTO_ODONTOLOGICO", uniqueConstraints = @UniqueConstraint(columnNames = "procedimento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComplementoOdontologico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Procedimento procedimento;

    /** Notação FDI (ISO 3950). */
    @Column(nullable = false, updatable = false)
    private int dente;

    /** Vazio: o dente inteiro. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_COMPLEMENTO_ODONTOLOGICO_FACE", joinColumns = @JoinColumn(name = "complemento_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "face", nullable = false)
    private Set<FaceDente> faces = new LinkedHashSet<>();
}
