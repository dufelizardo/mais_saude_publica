package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Ficha específica do agravo (ADR-0124): a definição dos campos, em JSON, versionada. Mudar a ficha cria a versão
 * seguinte; as notificações guardam a versão com que foram feitas.
 */
@Entity
@Table(name = "TB_FICHA_AGRAVO", uniqueConstraints = @UniqueConstraint(name = "uk_ficha_agravo_versao", columnNames = {"agravo_id", "versao"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FichaAgravo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agravo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Agravo agravo;

    @Column(nullable = false, updatable = false)
    private int versao;

    /** {"campos": [{"chave", "rotulo", "tipo", "obrigatorio", "opcoes"}]} */
    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String definicao;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
