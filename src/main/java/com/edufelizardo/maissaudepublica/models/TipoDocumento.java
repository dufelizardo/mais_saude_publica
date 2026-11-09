package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Tipo de documento (ADR-0172): catálogo editável. */
@Entity
@Table(name = "TB_TIPO_DOCUMENTO", uniqueConstraints = @UniqueConstraint(name = "uk_tipo_documento_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoDocumento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 60)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean ativo;
}
