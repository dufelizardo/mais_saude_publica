package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeComunicado;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoComunicado;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Comunicado (ADR-0168): o aviso operacional. Publicado não se edita; encerra com o motivo. Fora do período, sai do portal sozinho. */
@Entity
@Table(name = "TB_COMUNICADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Comunicado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeComunicado prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicoComunicacao publico;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_COMUNICADO_CANAL", joinColumns = @JoinColumn(name = "comunicado_id"), inverseJoinColumns = @JoinColumn(name = "canal_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CanalComunicacao> canais = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoComunicado situacao;

    private LocalDateTime publicadoEm;

    private String publicadoPorCpf;

    private LocalDate encerradoEm;

    @Column(length = 500)
    private String motivoEncerramento;

    @Column(updatable = false)
    private String registradoPorCpf;

    /** A campanha de onde o comunicado saiu, opcional (ADR-0174). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_comunicacao_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CampanhaComunicacao campanha;
}
