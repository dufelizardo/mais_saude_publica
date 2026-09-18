package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProfissionalRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "TB_PROFISSIONAL")
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

    /**
     * Chave única de verdade do domínio (ver ADR-0017) — gerada automaticamente no create(),
     * nunca informada pelo cliente. Uma recontratação cria uma ficha nova com matrícula nova,
     * em vez de reabrir a ficha antiga.
     */
    @Column(unique = true)
    private String matricula;

    /**
     * Não é mais único (ver ADR-0017): a mesma pessoa pode ter mais de uma ficha ao longo do
     * tempo, uma por vínculo empregatício. Toda consulta "por CPF" no service busca só a ficha
     * ATIVA (ver ProfissionalRepository.findByCpfAndAtivoTrue).
     */
    @NotBlank
    private String cpf;
    private String nome;
    private String conselhoClasse;
    private String numeroConselho;

    @Embedded
    private Endereco endereco;

    @ElementCollection
    @CollectionTable(name = "TB_TELEFONES_PROFISSIONAL")
    private Set<String> telefones;
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
        this.endereco = new Endereco(dto.getEndereco());
        this.telefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.dataAdmissao = dto.getDataAdmissao();
    }
}
