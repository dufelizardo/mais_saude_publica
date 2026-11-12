package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.CanalDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Denúncia à Vigilância Sanitária (ADR-0199). Pode ser anônima; o denunciante identificado só aparece para a coordenação. */
@Entity
@Table(name = "TB_DENUNCIA_SANITARIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DenunciaSanitaria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String protocolo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalDenuncia canal;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @Column(length = 300)
    private String estabelecimentoDescricao;

    @Column(nullable = false)
    private boolean anonima;

    @Column(length = 200)
    private String denuncianteNome;

    @Column(length = 200)
    private String denuncianteContato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDenuncia situacao;

    @Column(nullable = false, updatable = false)
    private Instant recebidaEm;

    @Column(length = 1000)
    private String conclusao;

    private Instant concluidaEm;

    private String concluidaPorCpf;

    @Column(updatable = false)
    private String registradoPorCpf;
}
