package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CategoriaSalarialRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Nível em que um dissídio se aplica (ver docs/rh/MODELO-RH.md, seção 2.1). Agrupa {@link Cargo}
 * pra fins de reajuste coletivo — não é a mesma coisa que {@code conselhoClasse} do
 * {@link Profissional} (CRM, COREN), que é registro profissional, não categoria salarial.
 */
@Entity
@Table(name = "TB_CATEGORIA_SALARIAL", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CategoriaSalarial implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String nome;

    private String convencaoColetiva;

    public CategoriaSalarial(CategoriaSalarialRequestDto dto) {
        this.nome = dto.getNome();
        this.convencaoColetiva = dto.getConvencaoColetiva();
    }
}
