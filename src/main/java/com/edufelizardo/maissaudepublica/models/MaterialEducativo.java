package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FormatoMaterial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Material educativo (ADR-0174): um formato do conteúdo. O arquivo vem com a D2 de Documentos; por ora, link externo opcional. */
@Entity
@Table(name = "TB_MATERIAL_EDUCATIVO", indexes = @Index(name = "ix_material_conteudo", columnList = "conteudo_grupo_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MaterialEducativo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(name = "conteudo_grupo_id", nullable = false, updatable = false)
    private UUID conteudoGrupoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormatoMaterial formato;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Column(length = 500)
    private String link;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    private LocalDateTime retiradoEm;

    @Column(length = 500)
    private String motivoRetirada;
}
