package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Responsabilidade que um {@link Profissional} assume sobre um {@link Setor} — distinta de estar
 * lotado nele (ver docs/adr/0035-responsabilidade-administrativa-separada-da-lotacao.md). Histórico
 * no padrão de {@link AjusteIndividual}/{@link AdesaoBeneficio}: permite múltiplas vigentes
 * simultâneas para o mesmo profissional, e encerrar é sempre preencher {@code dataFim}, nunca
 * deletar.
 */
@Entity
@Table(name = "TB_RESPONSABILIDADE_ADMINISTRATIVA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResponsabilidadeAdministrativa implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", nullable = false)
    private Setor setor;

    @NotBlank
    private String tipo;

    private String descricao;

    @NotNull
    private LocalDate dataInicio;

    private LocalDate dataFim;

    public ResponsabilidadeAdministrativa(Profissional profissional, Setor setor, String tipo, String descricao,
                                           LocalDate dataInicio) {
        this.profissional = profissional;
        this.setor = setor;
        this.tipo = tipo;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
    }
}
