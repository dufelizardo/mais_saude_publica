package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Catálogo de exames do laboratório assistencial (ADR-0093): material, preparo, prazo e como o resultado é dado —
 * numérico, com unidade e faixa de referência, ou texto. A faixa é copiada para cada resultado no registro, então
 * mudar o catálogo não muda resultado já dado. Não se apaga: sai de uso com {@code ativo = false}.
 */
@Entity
@Table(name = "TB_EXAME_LABORATORIAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameLaboratorial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(nullable = false, unique = true, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialExame material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoResultadoExame tipoResultado;

    @Column(length = 30)
    private String unidadeMedida;

    @Column(precision = 14, scale = 4)
    private BigDecimal referenciaMinima;

    @Column(precision = 14, scale = 4)
    private BigDecimal referenciaMaxima;

    /** Referência de exame em texto, por exemplo "Não reagente". */
    @Column(length = 200)
    private String referenciaTexto;

    @Column(length = 500)
    private String preparo;

    private Integer prazoDias;

    private boolean ativo;
}
