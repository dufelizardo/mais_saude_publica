package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceCondicaoOdontologica;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Condição do odontograma (ADR-0135, desenho na ADR-0111): hígido, cárie, restaurado, ausente… Catálogo cadastrável,
 * com a cor e a sigla do desenho e o alcance (face, dente inteiro ou os dois).
 */
@Entity
@Table(name = "TB_CONDICAO_ODONTOLOGICA", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CondicaoOdontologica implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcanceCondicaoOdontologica alcance;

    /** Cor no desenho (#RRGGBB). */
    @Column(nullable = false, length = 7)
    private String cor;

    /** Sigla no desenho (até 4 letras). */
    @Column(nullable = false, length = 4)
    private String sigla;

    private boolean ativa;
}
