package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FaixaRendaFamiliar;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Família morando num domicílio, com início e fim (ADR-0127). A mudança fecha o núcleo e abre outro no domicílio novo,
 * com os mesmos membros: fica o histórico de onde a família morou. O responsável é um membro vigente.
 */
@Entity
@Table(name = "TB_NUCLEO_FAMILIAR", indexes = @Index(name = "ix_nucleo_domicilio", columnList = "domicilio_id, fim"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NucleoFamiliar implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domicilio_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Domicilio domicilio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", nullable = false)
    private Paciente responsavel;

    @Enumerated(EnumType.STRING)
    private FaixaRendaFamiliar rendaFamiliar;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
