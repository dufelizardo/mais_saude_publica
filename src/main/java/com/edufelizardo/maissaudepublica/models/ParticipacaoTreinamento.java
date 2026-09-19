package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Participação de um {@link Profissional} num {@link Treinamento} (ver docs/rh/MODELO-RH.md,
 * seção 8). {@code dataValidade} é calculada pelo service a partir de
 * {@code treinamento.getValidadeMeses()} — não é informada pelo cliente.
 */
@Entity
@Table(name = "TB_PARTICIPACAO_TREINAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ParticipacaoTreinamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treinamento_id", referencedColumnName = "uuid", nullable = false)
    private Treinamento treinamento;

    @NotNull
    private LocalDate dataConclusao;

    private LocalDate dataValidade;

    private String certificadoUrl;

    public ParticipacaoTreinamento(Profissional profissional, Treinamento treinamento, LocalDate dataConclusao,
                                    LocalDate dataValidade, String certificadoUrl) {
        this.profissional = profissional;
        this.treinamento = treinamento;
        this.dataConclusao = dataConclusao;
        this.dataValidade = dataValidade;
        this.certificadoUrl = certificadoUrl;
    }
}
