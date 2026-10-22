package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.Parentesco;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pessoa de uma família, com início e fim (ADR-0127). A pessoa é o {@link Paciente} que já existe; um núcleo vigente por
 * paciente.
 */
@Entity
@Table(name = "TB_MEMBRO_NUCLEO", indexes = {
        @Index(name = "ix_membro_nucleo_nucleo", columnList = "nucleo_id, fim"),
        @Index(name = "ix_membro_nucleo_paciente", columnList = "paciente_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MembroNucleo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nucleo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private NucleoFamiliar nucleo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Parentesco parentesco;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoSaida;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
