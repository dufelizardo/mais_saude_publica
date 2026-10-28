package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoExterna;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Instituição de outro setor (assistência social, educação, garantia de direitos...) no catálogo único da
 * Intersetorialidade (ADR-0154), usado por todos os domínios que encaminham para fora da saúde. Não é unidade de saúde:
 * o sistema não implementa esses setores. Inativar tira do catálogo sem apagar os encaminhamentos.
 */
@Entity
@Table(name = "TB_INSTITUICAO_EXTERNA", uniqueConstraints = @UniqueConstraint(name = "uk_instituicao_externa_nome", columnNames = "nome"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InstituicaoExterna implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SetorIntersetorial setor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInstituicaoExterna tipo;

    @Column(length = 300)
    private String endereco;

    @Column(length = 30)
    private String telefone;

    @Column(length = 120)
    private String email;

    /** A pessoa de contato na instituição. */
    @Column(length = 120)
    private String responsavel;

    @Column(length = 500)
    private String observacao;

    @Column(nullable = false)
    private boolean ativa;

    @Column(nullable = false)
    private Instant atualizadaEm;

    private String registradoPorCpf;
}
