package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.RespostaCat;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMercadoTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcidenteTrabalho;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * O bloco do trabalho da notificação (ADR-0207): a ocupação e a situação na data do evento, o empregador, o local e, no
 * acidente, o tipo, a jornada, a CAT e o óbito. No acidente com material biológico, as datas do acompanhamento
 * sorológico realizadas. Quando o trabalhador é servidor da rede, liga-se ao acidente do RH, sem copiar a CAT.
 */
@Entity
@Table(name = "TB_EVENTO_TRABALHO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoTrabalho implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", nullable = false, updatable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Notificacao notificacao;

    @Column(nullable = false, length = 6)
    private String ocupacaoCbo;

    @Column(nullable = false, length = 200)
    private String ocupacaoDescricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoMercadoTrabalho situacaoTrabalho;

    @Column(length = 14)
    private String empregadorDocumento;

    @Column(length = 200)
    private String empregadorNome;

    @Column(length = 7)
    private String empregadorCnae;

    /** O empregador já cadastrado na vigilância sanitária (ADR-0199), pelo documento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @Column(length = 500)
    private String localEvento;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private TipoAcidenteTrabalho tipoAcidente;

    private Integer horasAposInicioJornada;

    @Enumerated(EnumType.STRING)
    private RespostaCat cat;

    private Integer outrosTrabalhadoresAtingidos;

    @Column(nullable = false)
    private boolean obito;

    /** O acidente do servidor no RH; um acidente, uma notificação. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acidente_trabalho_id", referencedColumnName = "uuid", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AcidenteTrabalho acidenteTrabalho;

    private LocalDate acompanhamentoSeisSemanas;

    private LocalDate acompanhamentoTresMeses;

    private LocalDate acompanhamentoSeisMeses;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    private Instant atualizadoEm;

    private String atualizadoPorCpf;
}
