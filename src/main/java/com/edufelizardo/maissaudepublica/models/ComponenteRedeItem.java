package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRedeTematica;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Liga um componente de rede temática a um perfil ou a uma capacidade do modelo operacional (GR2, ADR-0197): a unidade com o perfil ou a
 * capacidade vigente é ponto daquele componente. Uma capacidade pode estar em várias redes.
 */
@Entity
@Table(name = "TB_COMPONENTE_REDE_ITEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ComponenteRedeItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ComponenteRedeTematica componente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PerfilOperacional perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capacidade_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CapacidadeAssistencial capacidade;
}
