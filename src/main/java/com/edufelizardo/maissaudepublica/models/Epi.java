package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Equipamento de Proteção Individual entregue a um {@link Profissional} (ver
 * docs/rh/MODELO-RH.md, seção 9, SST). {@code dataDevolucao} nula = ainda com o profissional.
 */
@Entity
@Table(name = "TB_EPI")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Epi implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotBlank
    private String tipo;

    private String numeroCA;

    @NotNull
    private LocalDate dataEntrega;

    private LocalDate dataDevolucao;

    public Epi(Profissional profissional, String tipo, String numeroCA, LocalDate dataEntrega, LocalDate dataDevolucao) {
        this.profissional = profissional;
        this.tipo = tipo;
        this.numeroCA = numeroCA;
        this.dataEntrega = dataEntrega;
        this.dataDevolucao = dataDevolucao;
    }
}
