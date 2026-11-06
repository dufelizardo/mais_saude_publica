package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço da unidade (ADR-0165): como a capacidade está organizada ali, por exemplo a "UTI Adulto" do hospital, com os
 * setores que a compõem. Tem histórico: encerrado, fica com a data e o motivo.
 */
@Entity
@Table(name = "TB_SERVICO_UNIDADE", indexes = @Index(name = "ix_servico_unidade_unidade", columnList = "unidade_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ServicoUnidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoServico tipo;

    @Column(nullable = false, length = 150)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional responsavel;

    @Column(nullable = false)
    private boolean vinteQuatroHoras;

    @Column(length = 200)
    private String horario;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_SERVICO_SETOR", joinColumns = @JoinColumn(name = "servico_id"), inverseJoinColumns = @JoinColumn(name = "setor_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Setor> setores = new HashSet<>();

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
