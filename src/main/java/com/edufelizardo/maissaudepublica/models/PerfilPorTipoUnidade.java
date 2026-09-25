package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Configuração que resolve qual {@link PerfilAdministrativo} vale para cada
 * {@link TipoUnidadeDeSaude} (ver docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md) — uma
 * linha por tipo, sem condicional no código. Sem override por unidade individual nesta fase.
 */
@Entity
@Table(name = "TB_PERFIL_POR_TIPO_UNIDADE", uniqueConstraints = {
        @UniqueConstraint(columnNames = "tipo")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PerfilPorTipoUnidade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private TipoUnidadeDeSaude tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_administrativo_id", referencedColumnName = "uuid", nullable = false)
    private PerfilAdministrativo perfilAdministrativo;

    public PerfilPorTipoUnidade(TipoUnidadeDeSaude tipo, PerfilAdministrativo perfilAdministrativo) {
        this.tipo = tipo;
        this.perfilAdministrativo = perfilAdministrativo;
    }
}
