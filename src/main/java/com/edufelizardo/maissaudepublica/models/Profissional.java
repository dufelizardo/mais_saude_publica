package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "TB_PROFISSIONAL", uniqueConstraints = {
        @UniqueConstraint(columnNames = "cpf")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Profissional implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String cpf;
    private String nome;
    private String conselhoClasse;
    private String numeroConselho;
    private String telefone;
    private String email;
    private LocalDate dataAdmissao;
    private LocalDate dataDesligamento;
    private boolean ativo;

    public Profissional(ProfissionalRequestDto dto) {
        this.ativo = true;
        this.cpf = dto.getCpf();
        this.nome = dto.getNome();
        this.conselhoClasse = dto.getConselhoClasse();
        this.numeroConselho = dto.getNumeroConselho();
        this.telefone = dto.getTelefone();
        this.email = dto.getEmail();
        this.dataAdmissao = dto.getDataAdmissao();
    }
}
