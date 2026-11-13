package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoInspecao;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoInspecao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Inspeção sanitária (ADR-0199): os fiscais, o roteiro respondido, o resultado e o relatório, assinados pelo fiscal logado (CPF e SHA-256, como na ADR-0183). */
@Entity
@Table(name = "TB_INSPECAO_SANITARIA", indexes = @Index(name = "ix_inspecao_sanitaria_estabelecimento", columnList = "estabelecimento_id, data_inspecao"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InspecaoSanitaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoInspecao motivo;

    @Column(nullable = false, updatable = false)
    private LocalDate dataInspecao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denuncia_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DenunciaSanitaria denuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surto_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Surto surto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspecao_origem_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InspecaoSanitaria inspecaoOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roteiro_versao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VersaoRoteiroInspecao roteiroVersao;

    /** {"codigo": "CONFORME" | "NAO_CONFORME" | "NAO_SE_APLICA"} */
    @Column(columnDefinition = "text", updatable = false)
    private String respostas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoInspecao resultado;

    @Column(nullable = false, length = 4000, updatable = false)
    private String relatorio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_INSPECAO_SANITARIA_FISCAL", joinColumns = @JoinColumn(name = "inspecao_id"), inverseJoinColumns = @JoinColumn(name = "profissional_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Profissional> fiscais = new LinkedHashSet<>();

    private String assinadoPorCpf;

    @Column(length = 64)
    private String hashConteudo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
