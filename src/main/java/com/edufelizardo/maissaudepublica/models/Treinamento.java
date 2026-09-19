package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TreinamentoRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Catálogo de treinamentos (ver docs/rh/MODELO-RH.md, seção 8). {@code validadeMeses} nulo
 * significa treinamento sem validade (não expira) — usado por {@link ParticipacaoTreinamento}
 * pra calcular a data de validade da participação.
 */
@Entity
@Table(name = "TB_TREINAMENTO", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Treinamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String nome;

    private Integer cargaHoraria;

    private Integer validadeMeses;

    private boolean obrigatorio;

    public Treinamento(TreinamentoRequestDto dto) {
        this.nome = dto.getNome();
        this.cargaHoraria = dto.getCargaHoraria();
        this.validadeMeses = dto.getValidadeMeses();
        this.obrigatorio = dto.getObrigatorio();
    }
}
