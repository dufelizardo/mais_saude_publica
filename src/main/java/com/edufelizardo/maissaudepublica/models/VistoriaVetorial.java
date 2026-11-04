package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoVistoriaVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVistoriaVetorial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Vistoria do agente de endemias a um imóvel do Território (ADR-0162): o resultado, os depósitos inspecionados por tipo do PNCD, o tratamento focal e os focos. O ciclo bimestral é guardado para os índices. Só inclusão. */
@Entity
@Table(name = "TB_VISTORIA_VETORIAL", indexes = {@Index(name = "ix_vistoria_vetorial_ciclo", columnList = "ciclo_ano, ciclo_numero"), @Index(name = "ix_vistoria_vetorial_imovel", columnList = "imovel_id, data")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VistoriaVetorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imovel_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Imovel imovel;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(name = "ciclo_ano", nullable = false, updatable = false)
    private int cicloAno;

    @Column(name = "ciclo_numero", nullable = false, updatable = false)
    private int cicloNumero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profissional agente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoVistoriaVetorial tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ResultadoVistoriaVetorial resultado;

    @Column(name = "depositos_a1", nullable = false, updatable = false)
    private int depositosA1;
    @Column(name = "depositos_a2", nullable = false, updatable = false)
    private int depositosA2;
    @Column(name = "depositos_b", nullable = false, updatable = false)
    private int depositosB;
    @Column(name = "depositos_c", nullable = false, updatable = false)
    private int depositosC;
    @Column(name = "depositos_d1", nullable = false, updatable = false)
    private int depositosD1;
    @Column(name = "depositos_d2", nullable = false, updatable = false)
    private int depositosD2;
    @Column(name = "depositos_e", nullable = false, updatable = false)
    private int depositosE;

    @Column(nullable = false, updatable = false)
    private int depositosEliminados;

    @Column(nullable = false, updatable = false)
    private int depositosTratados;

    @Column(length = 100, updatable = false)
    private String larvicida;

    @Column(updatable = false)
    private Double larvicidaGramas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AcaoControleVetorial acao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    @OneToMany(mappedBy = "vistoria", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tipoDeposito")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<FocoVetorial> focos = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
