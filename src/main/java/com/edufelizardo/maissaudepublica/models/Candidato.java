package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusCandidato;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Candidato inscrito numa {@link Vaga} (ver docs/rh/MODELO-RH.md, seção 10). {@code cpf} não é
 * único — mesma decisão da ADR-0017 (fichas de {@link Profissional}): nada impede a mesma pessoa
 * de se candidatar a vagas diferentes, ou de novo depois de reprovada. Candidato aprovado gerar
 * um {@code Profissional} novo (via {@code POST /api/v1/profissional/} já existente) é uma
 * orquestração fora do escopo desta entidade.
 */
@Entity
@Table(name = "TB_CANDIDATO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Candidato implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", referencedColumnName = "uuid", nullable = false)
    private Vaga vaga;

    @NotBlank
    private String nome;

    @NotBlank
    private String cpf;

    private String curriculoUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusCandidato status;

    public Candidato(Vaga vaga, String nome, String cpf, String curriculoUrl, StatusCandidato status) {
        this.vaga = vaga;
        this.nome = nome;
        this.cpf = cpf;
        this.curriculoUrl = curriculoUrl;
        this.status = status;
    }
}
