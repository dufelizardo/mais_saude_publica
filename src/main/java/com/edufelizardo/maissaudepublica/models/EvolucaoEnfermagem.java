package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Nota de evolução de enfermagem registrada ao longo de um {@link Atendimento} — segunda entidade
 * do domínio Enfermagem (ver MAPA-DE-DOMINIOS.md #8, ADR-0048). Vinculada a {@code Atendimento}, não
 * a {@link Consulta}: o acompanhamento é contínuo ao longo do atendimento, não amarrado a um evento
 * de consulta específico (ver ADR-0048). Mesmo padrão de FK por matrícula de {@link Triagem}/
 * {@link Consulta} (ADR-0034/ADR-0041).
 */
@Entity
@Table(name = "TB_EVOLUCAO_ENFERMAGEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EvolucaoEnfermagem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    private LocalDateTime dataHora;

    @NotBlank
    private String descricao;

    public EvolucaoEnfermagem(Atendimento atendimento, Profissional profissional, LocalDateTime dataHora,
                               String descricao) {
        this.atendimento = atendimento;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.descricao = descricao;
    }
}
