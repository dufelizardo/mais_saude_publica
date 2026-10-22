package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.EtapaProtetica;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Uma etapa registrada do tratamento protético (ADR-0139): quando foi moldado, quando voltou do laboratório. */
@Entity
@Table(name = "TB_ETAPA_TRATAMENTO_PROTETICO", indexes = @Index(name = "ix_etapa_protetica_tratamento", columnList = "tratamento_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EtapaTratamentoProtetico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tratamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private TratamentoProtetico tratamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private EtapaProtetica etapa;

    @Column(nullable = false, updatable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @Column(length = 500, updatable = false)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
