package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.GrauRiscoSanitario;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Atividade econômica (CNAE) do estabelecimento regulado, com o grau de risco sanitário (RDC 153/2017; ADR-0199). */
@Entity
@Table(name = "TB_ATIVIDADE_ESTABELECIMENTO", uniqueConstraints = @UniqueConstraint(name = "uk_atividade_estabelecimento_cnae", columnNames = {"estabelecimento_id", "cnae"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtividadeEstabelecimento implements Serializable {
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

    @Column(nullable = false, length = 7)
    private String cnae;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrauRiscoSanitario risco;
}
