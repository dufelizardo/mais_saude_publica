package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Concede um {@link Papel} a um {@link Usuario} num escopo (ADR-0054, ADR-0066). O escopo é uma
 * {@link UnidadeDeSaude} e vale para ela e para as unidades abaixo dela na hierarquia; sem unidade, vale
 * para a rede inteira. {@code inicio}/{@code fim} opcionais dão acesso temporário (ex.: plantão).
 *
 * <p>Não é apagada: é revogada, com motivo, autor e hora — o histórico de quem teve acesso a quê fica.
 */
@Entity
@Table(name = "TB_ATRIBUICAO_ACESSO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtribuicaoAcesso implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "papel_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Papel papel;

    /** Escopo; nulo = rede inteira. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", updatable = false)
    private UnidadeDeSaude unidade;

    @Column(updatable = false)
    private LocalDate inicio;

    @Column(updatable = false)
    private LocalDate fim;

    @NotNull
    @Column(updatable = false)
    private Instant concedidoEm;

    @Column(updatable = false)
    private String concedidoPorCpf;

    private Instant revogadoEm;

    private String revogadoPorCpf;

    @Column(length = 1000)
    private String motivoRevogacao;

    /** Vigente na data: não revogada e dentro do período, quando houver. */
    public boolean vigenteEm(LocalDate data) {
        return revogadoEm == null
                && (inicio == null || !data.isBefore(inicio))
                && (fim == null || !data.isAfter(fim));
    }
}
