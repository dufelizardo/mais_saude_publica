package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * As vagas e a permanência máxima do acolhimento noturno da unidade (ADR-0153), editáveis pela gestão da rede.
 */
@Entity
@Table(name = "TB_CONFIGURACAO_ACOLHIMENTO_NOTURNO", uniqueConstraints = @UniqueConstraint(name = "uk_configuracao_acolhimento_unidade", columnNames = "unidade_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConfiguracaoAcolhimentoNoturno implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Column(nullable = false)
    private int vagas;

    @Column(nullable = false)
    private int permanenciaMaximaDias;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private String registradoPorCpf;
}
