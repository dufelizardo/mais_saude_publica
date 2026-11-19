package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Empenho da despesa (Lei 4.320/1964, art. 58 a 61; ADR-0218): o número do SIAFIC, o tipo, a dotação, o fornecedor e, se houver, a ata
 * ou o contrato; os reforços e as anulações ficam em {@link MovimentoEmpenho}. */
@Entity
@Table(name = "TB_EMPENHO", uniqueConstraints = @UniqueConstraint(name = "uk_empenho_numero", columnNames = {"exercicio", "numero"}), indexes = {@Index(name = "ix_empenho_dotacao", columnList = "dotacao_id"), @Index(name = "ix_empenho_fornecedor", columnList = "fornecedor_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Empenho implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 30, updatable = false)
    private String numero;

    @Column(nullable = false, updatable = false)
    private int exercicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEmpenho tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dotacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DotacaoOrcamentaria dotacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstrumentoContratual instrumento;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valor;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @Column(length = 500, updatable = false)
    private String historico;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
