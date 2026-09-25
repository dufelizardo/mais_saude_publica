package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PacienteRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Identidade da pessoa atendida pela rede pública de saúde — primeira entidade da onda
 * "Operação Assistencial" (ver ADR-0039, MAPA-DE-DOMINIOS.md #5). Sem {@code usuario_id}
 * (não há autenticação — decisão mantida na ADR-0039) e sem entidade {@code Pessoa}
 * compartilhada com {@link Profissional} (YAGNI, mesmo raciocínio da ADR-0034): o caso raro de
 * alguém ser funcionário e paciente ao mesmo tempo fica resolvido por vínculo fraco por CPF, se um
 * dia for necessário — mesmo princípio da ADR-0014.
 */
@Entity
@Table(name = "TB_PACIENTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Paciente implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    private String nome;

    /**
     * Não é único — mesmo motivo do {@link Profissional} (ver ADR-0017): uma pessoa pode acabar
     * com mais de um registro ao longo do tempo (unidades diferentes, cadastros duplicados).
     */
    private String cpf;

    /**
     * Cartão Nacional de Saúde (CNS) — identificador de referência externo, opcional. Ainda não é
     * a chave usada por outros domínios para referenciar Paciente (isso continua sendo o
     * {@code uuid}, ver DER.md) — joga papel equivalente ao da matrícula do Profissional só no
     * sentido de ser o identificador "de fora" do sistema, não uma FK interna.
     */
    private String cartaoSus;

    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Embedded
    private Endereco endereco;

    @ElementCollection
    @CollectionTable(name = "TB_TELEFONES_PACIENTE")
    private Set<String> telefones;

    private String email;
    private boolean ativo;

    public Paciente(PacienteRequestDto dto) {
        this.nome = dto.getNome();
        this.cpf = dto.getCpf();
        this.cartaoSus = dto.getCartaoSus();
        this.dataNascimento = dto.getDataNascimento();
        this.sexo = dto.getSexo();
        this.endereco = new Endereco(dto.getEndereco());
        this.telefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.ativo = dto.getAtivo();
    }
}
