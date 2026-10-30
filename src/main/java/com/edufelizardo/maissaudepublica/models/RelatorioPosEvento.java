package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRelatorioPosEvento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** O relatório pós-evento da emergência (ADR-0159): o que a equipe escreve; os números são calculados na leitura. Um por emergência; finalizado não muda. */
@Entity
@Table(name = "TB_RELATORIO_POS_EVENTO", uniqueConstraints = @UniqueConstraint(name = "uk_relatorio_pos_evento_emergencia", columnNames = "emergencia_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RelatorioPosEvento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergencia_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Emergencia emergencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoRelatorioPosEvento situacao;

    @Column(length = 4000)
    private String resumo;

    @Column(length = 4000)
    private String problemas;

    @Column(length = 4000)
    private String licoes;

    @OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RecomendacaoRelatorio> recomendacoes = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(nullable = false)
    private Instant atualizadoEm;

    private Instant finalizadoEm;

    private String finalizadoPorCpf;

    @Column(updatable = false)
    private String registradoPorCpf;
}
