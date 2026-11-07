package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPublicacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Publicação (ADR-0168): o que saiu, em qual canal, quando e com qual resultado. Só inclusão. */
@Entity
@Table(name = "TB_PUBLICACAO", indexes = {@Index(name = "ix_publicacao_comunicado", columnList = "comunicado_id"), @Index(name = "ix_publicacao_conteudo", columnList = "conteudo_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Publicacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicado_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Comunicado comunicado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteudo_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ConteudoEducativo conteudo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CanalComunicacao canal;

    @Column(nullable = false, updatable = false)
    private LocalDateTime publicadaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private SituacaoPublicacao situacao;

    @Column(nullable = false, updatable = false)
    private int enviados;

    @Column(nullable = false, updatable = false)
    private int falhas;

    @Column(length = 300, updatable = false)
    private String detalhe;

    @Column(updatable = false)
    private String publicadaPorCpf;
}
