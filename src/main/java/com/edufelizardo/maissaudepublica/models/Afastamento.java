package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ausência de um {@link Profissional} — férias, licença médica/pessoal (ver
 * docs/rh/MODELO-RH.md, seção 3). Tipos legais específicos com regras próprias de duração/
 * remuneração (maternidade, acidente de trabalho) são {@link Licenca} (Fase 3), não aqui.
 *
 * <p>Regra confirmada no desenho: Afastamento NÃO altera {@link Profissional#isAtivo()} —
 * {@code ativo}/{@code dataDesligamento} continua reservado exclusivamente pro desligamento
 * (ADR-0017). Afastado continua ativo, só temporariamente ausente.
 */
@Entity
@Table(name = "TB_AFASTAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Afastamento implements Serializable {
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
    private TipoAfastamento tipo;

    @NotNull
    private LocalDate dataInicio;

    @NotNull
    private LocalDate dataFim;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusAfastamento status;

    private String observacao;

    public Afastamento(Profissional profissional, TipoAfastamento tipo, LocalDate dataInicio,
                        LocalDate dataFim, StatusAfastamento status, String observacao) {
        this.profissional = profissional;
        this.tipo = tipo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.observacao = observacao;
    }
}
