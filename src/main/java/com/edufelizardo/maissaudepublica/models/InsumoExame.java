package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Um insumo de coleta ou de realização do exame (ADR-0183): o material do Estoque e a quantidade. Na mesma amostra, cada
 * insumo conta uma vez, pela maior quantidade entre os exames dela. "Só com contraste" vale para o exame de imagem.
 */
@Entity
@Table(name = "TB_INSUMO_EXAME", uniqueConstraints = @UniqueConstraint(name = "uk_insumo_exame_material", columnNames = {"exame_id", "material_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InsumoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exame_id", referencedColumnName = "uuid", nullable = false)
    private ExameLaboratorial exame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", referencedColumnName = "uuid", nullable = false)
    private MaterialEstoque material;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private boolean soComContraste;
}
