package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.CategoriaMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeMedidaMaterial;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Material do almoxarifado (ADR-0177): insumo geral, fora da Farmácia e da Sala de Vacina. Código e nome únicos. */
@Entity
@Table(name = "TB_MATERIAL_ESTOQUE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MaterialEstoque implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    /** Código do Catálogo de Materiais do governo federal, quando houver. */
    @Column(length = 20)
    private String catmat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaMaterial categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadeMedidaMaterial unidadeMedida;

    /** Com lote e validade (gaze, seringa); sem, um saldo só por unidade (papel). Não muda depois da primeira entrada. */
    @Column(nullable = false)
    private boolean controlaLote;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo;
}
