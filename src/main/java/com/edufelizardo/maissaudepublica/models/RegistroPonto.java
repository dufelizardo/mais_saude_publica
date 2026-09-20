package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroPonto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de jornada de um {@link Profissional} (ver docs/rh/MODELO-RH.md, seção 4). Depende
 * conceitualmente de {@code Lotacao.jornadaSemanalHoras} (Fase 0) pra saber a jornada esperada e
 * de {@link Afastamento} (Fase 1) pra não cobrar ponto de quem está afastado — mas esse
 * cruzamento é lógica de cálculo/consolidação, fora do escopo desta entidade.
 */
@Entity
@Table(name = "TB_REGISTRO_PONTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroPonto implements Serializable {
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

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoRegistroPonto tipo;

    private String origem;

    /**
     * Correção pendente de aprovação do gestor (ver docs/rh/MODELO-RH.md, seção 4). "Pendente" é
     * derivado de {@code dataHoraProposta != null} -- não existe um campo de status separado,
     * mesmo padrão já usado em situações derivadas de outros subdomínios (EPI, ciclo de
     * avaliação). Aprovar aplica a proposta em dataHora/tipo e limpa estes 3 campos; rejeitar só
     * limpa, mantendo o registro original.
     */
    private LocalDateTime dataHoraProposta;
    @Enumerated(EnumType.STRING)
    private TipoRegistroPonto tipoProposto;
    private String justificativaCorrecao;

    public RegistroPonto(Profissional profissional, LocalDateTime dataHora, TipoRegistroPonto tipo, String origem) {
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.origem = origem;
    }
}
