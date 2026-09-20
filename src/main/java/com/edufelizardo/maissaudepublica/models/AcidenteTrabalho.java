package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Acidente de trabalho de um {@link Profissional} (ver docs/rh/MODELO-RH.md, seção 9, SST).
 * O desenho menciona gerar um {@link Afastamento} (Fase 1) quando há {@code diasAfastamento} —
 * essa composição não faz parte desta fatia, mesma decisão de sempre: só o registro.
 */
@Entity
@Table(name = "TB_ACIDENTE_TRABALHO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcidenteTrabalho implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    private LocalDateTime dataHora;

    private String descricao;

    private boolean catEmitida;

    private String catUrl;

    private Integer diasAfastamento;

    public AcidenteTrabalho(Profissional profissional, LocalDateTime dataHora, String descricao,
                             boolean catEmitida, String catUrl, Integer diasAfastamento) {
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.descricao = descricao;
        this.catEmitida = catEmitida;
        this.catUrl = catUrl;
        this.diasAfastamento = diasAfastamento;
    }
}
