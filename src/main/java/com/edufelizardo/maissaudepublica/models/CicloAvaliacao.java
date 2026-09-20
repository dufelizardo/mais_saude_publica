package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CicloAvaliacaoRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ciclo de avaliação de desempenho, ex.: "2027-S1" (ver docs/rh/MODELO-RH.md, seção 11).
 */
@Entity
@Table(name = "TB_CICLO_AVALIACAO", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CicloAvaliacao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String nome;

    @NotNull
    private LocalDate dataInicio;

    @NotNull
    private LocalDate dataFim;

    public CicloAvaliacao(CicloAvaliacaoRequestDto dto) {
        this.nome = dto.getNome();
        this.dataInicio = dto.getDataInicio();
        this.dataFim = dto.getDataFim();
    }
}
