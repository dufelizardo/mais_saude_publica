package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaTipoEmergencia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Tipo de emergência (ADR-0156): catálogo cadastrável; o desastre guarda o código COBRADE. */
@Entity
@Table(name = "TB_TIPO_EMERGENCIA", uniqueConstraints = @UniqueConstraint(name = "uk_tipo_emergencia_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoEmergencia implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaTipoEmergencia categoria;

    /** Classificação e Codificação Brasileira de Desastres (ex.: 1.2.1.0.0). */
    @Column(length = 20)
    private String cobrade;

    @Column(nullable = false)
    private boolean ativo;
}
