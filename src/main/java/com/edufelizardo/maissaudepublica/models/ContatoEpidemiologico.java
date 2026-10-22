package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContato;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExposicaoContato;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contato de um caso (ADR-0130, desenho na ADR-0110): a pessoa exposta, com ou sem cadastro (nome, telefone, endereço),
 * o tipo e a data da exposição e o acompanhamento até o fim do período de monitoramento do agravo. Adoecendo, ganha a
 * própria notificação, ligada aqui.
 */
@Entity
@Table(name = "TB_CONTATO_EPIDEMIOLOGICO", indexes = @Index(name = "ix_contato_notificacao", columnList = "notificacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ContatoEpidemiologico implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** O caso índice. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Notificacao notificacao;

    /** O paciente, quando há cadastro (pode vir depois). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid")
    private Paciente paciente;

    /** Sem cadastro: como se chama. */
    @Column(length = 200)
    private String nome;

    @Column(length = 40)
    private String telefone;

    @Column(length = 500)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoExposicaoContato tipoExposicao;

    private LocalDate dataExposicao;

    /** Exposição mais o período de monitoramento do agravo; nulo quando o agravo não define. */
    private LocalDate fimMonitoramento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoContato situacao;

    @Column(length = 1000)
    private String observacao;

    /** A notificação do contato, quando ele virou caso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacao_gerada_id", referencedColumnName = "uuid")
    private Notificacao notificacaoGerada;

    @Column(nullable = false)
    private Instant atualizadoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
