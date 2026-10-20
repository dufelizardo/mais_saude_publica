package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.PapelRedeApoio;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pessoa ou serviço da rede de apoio do caso (ADR-0121), como histórico: sair da rede encerra com motivo.
 */
@Entity
@Table(name = "TB_PESSOA_REDE_APOIO", indexes = @Index(name = "ix_pessoa_rede_apoio_acomp", columnList = "acompanhamento_id, fim"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PessoaRedeApoio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @Column(nullable = false, length = 200, updatable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PapelRedeApoio papel;

    /** Ex.: mãe, vizinha, CRAS Centro. */
    @Column(length = 100, updatable = false)
    private String vinculo;

    @Column(length = 30, updatable = false)
    private String telefone;

    @Column(length = 500, updatable = false)
    private String observacao;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Column(length = 500)
    private String motivoFim;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
