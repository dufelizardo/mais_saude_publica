package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vínculo profissional↔unidade↔cargo com histórico (ver docs/rh/MODELO-RH.md, seção 2.4). No
 * máximo uma {@code Lotacao} com {@code dataFim = null} por {@link Profissional} (ficha) por vez —
 * regra garantida pelo {@code LotacaoService}, não pelo schema. Transferir unidade ou trocar de
 * cargo é sempre fechar a vigente + criar uma nova, nunca um update nesta entidade.
 */
@Entity
@Table(name = "TB_LOTACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Lotacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", referencedColumnName = "uuid", nullable = false)
    private Cargo cargo;

    private Integer jornadaSemanalHoras;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    private String motivo;

    public Lotacao(Profissional profissional, UnidadeDeSaude unidade, Cargo cargo,
                    Integer jornadaSemanalHoras, LocalDate dataInicio, String motivo) {
        this.profissional = profissional;
        this.unidade = unidade;
        this.cargo = cargo;
        this.jornadaSemanalHoras = jornadaSemanalHoras;
        this.dataInicio = dataInicio;
        this.motivo = motivo;
    }
}
