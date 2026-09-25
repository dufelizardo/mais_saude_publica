package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Necessidade operacional de pessoal registrada pela Administração — mais granular que {@link Vaga}
 * (por setor, com jornada/competências/justificativa) e não a substitui (ver
 * docs/adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md). {@code vagaAssociada} é um link
 * informativo preenchido depois, quando o RH decide abrir recrutamento a partir desta necessidade —
 * nunca um gatilho automático.
 */
@Entity
@Table(name = "TB_NECESSIDADE_DE_PESSOAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NecessidadeDePessoal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", nullable = true)
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", referencedColumnName = "uuid", nullable = false)
    private Cargo cargo;

    @NotNull
    private Integer quantidade;

    @NotNull
    private Integer jornadaSemanalHoras;

    private String competenciasNecessarias;

    private String justificativa;

    @NotNull
    private LocalDate dataRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_associada_id", referencedColumnName = "uuid", nullable = true)
    private Vaga vagaAssociada;

    public NecessidadeDePessoal(UnidadeDeSaude unidade, Setor setor, Cargo cargo, Integer quantidade,
                                 Integer jornadaSemanalHoras, String competenciasNecessarias,
                                 String justificativa, LocalDate dataRegistro) {
        this.unidade = unidade;
        this.setor = setor;
        this.cargo = cargo;
        this.quantidade = quantidade;
        this.jornadaSemanalHoras = jornadaSemanalHoras;
        this.competenciasNecessarias = competenciasNecessarias;
        this.justificativa = justificativa;
        this.dataRegistro = dataRegistro;
    }
}
