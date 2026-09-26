package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Identidade de login — primeira entidade do domínio Identidade/Autorização (ver
 * MAPA-DE-DOMINIOS.md #20, ADR-0054, ADR-0055). Separada de {@link Profissional} e
 * {@link Paciente} por design (ADR-0054 decisão 1): {@code cpf} aqui é próprio e único, uma pessoa
 * real = uma identidade de login, independente de quantos registros históricos de
 * {@code Profissional} essa mesma pessoa tenha (ADR-0017 tornou {@code Profissional.cpf}
 * não-único por outro motivo — reconciliação de múltiplos vínculos empregatícios — o que não
 * conflita com a unicidade aqui). O vínculo com {@code Profissional} é resolvido em tempo de login
 * via {@code ProfissionalRepository.findByCpfAndAtivoTrue}, mesmo padrão de vínculo fraco por CPF
 * da ADR-0014, sem FK direta.
 *
 * <p>Sem entidade {@code Credencial} separada nesta fase (ADR-0055) — só existe um tipo de
 * credencial (senha) até "Entrar com gov.br" sair do estado "Em breve".
 *
 * <p>Sem {@code Papel}/{@code Permissao}/{@code EscopoAcesso} nesta fase — este é só o corte de
 * autenticação (você está logado ou não) da ADR-0054; autorização granular fica para quando houver
 * um consumidor real (ver ADR-0055).
 */
@Entity
@Table(name = "TB_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Usuario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String cpf;

    @NotBlank
    private String nome;

    @NotBlank
    @ToString.Exclude
    private String senhaHash;

    private boolean ativo = true;

    /**
     * Zerado a cada login bem-sucedido; usado junto com {@link #bloqueadoAte} para o bloqueio
     * temporário após tentativas inválidas (ver aviso de segurança do mockup de Login, ADR-0055).
     */
    private int tentativasFalhas;

    private Instant bloqueadoAte;

    private Instant ultimoAcessoEm;

    public Usuario(String cpf, String nome, String senhaHash) {
        this.cpf = cpf;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.ativo = true;
    }
}
