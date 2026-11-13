package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Checklist de auditoria de adesão a um protocolo (ADR-0205): os itens ficam nas versões, como o roteiro da VISA. */
@Entity
@Table(name = "TB_CHECKLIST_PROTOCOLO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ChecklistProtocolo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 40, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;
}
