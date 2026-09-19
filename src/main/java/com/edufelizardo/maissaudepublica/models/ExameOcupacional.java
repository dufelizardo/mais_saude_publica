package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoExameOcupacional;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExameOcupacional;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Exame ocupacional de um {@link Profissional} — admissional, periódico, demissional, etc. (ver
 * docs/rh/MODELO-RH.md, seção 9, SST).
 */
@Entity
@Table(name = "TB_EXAME_OCUPACIONAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameOcupacional implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoExameOcupacional tipo;

    @NotNull
    private LocalDate dataRealizacao;

    private LocalDate dataValidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ResultadoExameOcupacional resultado;

    private String asoUrl;

    public ExameOcupacional(Profissional profissional, TipoExameOcupacional tipo, LocalDate dataRealizacao,
                             LocalDate dataValidade, ResultadoExameOcupacional resultado, String asoUrl) {
        this.profissional = profissional;
        this.tipo = tipo;
        this.dataRealizacao = dataRealizacao;
        this.dataValidade = dataValidade;
        this.resultado = resultado;
        this.asoUrl = asoUrl;
    }
}
