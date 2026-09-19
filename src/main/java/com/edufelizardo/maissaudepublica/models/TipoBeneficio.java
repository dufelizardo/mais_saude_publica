package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoBeneficioRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.CusteioBeneficio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Catálogo de benefícios — remuneração indireta, não é salário (ver docs/rh/MODELO-RH.md, seção
 * 2.7). {@code custeio} define quem paga: EMPRESA (ex.: VR/VA, plano de saúde), COMPARTILHADO
 * (ex.: VT, com desconto limitado por lei) ou PROFISSIONAL.
 */
@Entity
@Table(name = "TB_TIPO_BENEFICIO", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TipoBeneficio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CusteioBeneficio custeio;

    public TipoBeneficio(TipoBeneficioRequestDto dto) {
        this.nome = dto.getNome();
        this.custeio = dto.getCusteio();
    }
}
