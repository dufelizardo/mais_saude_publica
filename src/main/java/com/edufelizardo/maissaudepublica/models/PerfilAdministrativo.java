package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Configuração administrativa aplicável a um tipo de unidade de saúde (ver
 * docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md) — ex.: {@code ADMIN_UBS},
 * {@code ADMIN_HOSPITAL}. Não é uma implementação diferente do setor administrativo, só
 * configuração; a relação com {@link com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude}
 * vive em {@link PerfilPorTipoUnidade}, não como campo aqui.
 */
@Entity
@Table(name = "TB_PERFIL_ADMINISTRATIVO", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerfilAdministrativo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String codigo;

    @NotBlank
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private boolean ativo;

    public PerfilAdministrativo(String codigo, String nome, String descricao, boolean ativo) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
    }
}
