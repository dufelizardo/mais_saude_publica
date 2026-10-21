package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Local da campanha (ADR-0133): uma unidade da rede, ou um local temporário (escola, praça, unidade móvel) com a unidade
 * responsável por ele — é ela que define quem registra as ações ali.
 */
@Entity
@Table(name = "TB_LOCAL_CAMPANHA", indexes = @Index(name = "ix_local_campanha", columnList = "campanha_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LocalCampanha implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Campanha campanha;

    /** A unidade da rede, ou a responsável pelo local temporário. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    private boolean temporario;

    /** Local temporário: nome, endereço e o ponto no mapa (opcional). */
    @Column(length = 200)
    private String nome;

    @Column(length = 500)
    private String endereco;

    private Double latitude;

    private Double longitude;

    /** Dias e horário ("seg a sex, 8h às 17h"). */
    @Column(length = 200)
    private String funcionamento;

    /** Período próprio, dentro do da campanha (opcional). */
    private LocalDate inicio;

    private LocalDate fim;
}
