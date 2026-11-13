package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Uma emissão do laudo em PDF (ADR-0183): os resultados incluídos, o PDF guardado como saiu, o hash dele e o código de
 * verificação pública. Baixar de novo sem resultado novo devolve a mesma emissão; com resultado novo, retificação ou adendo,
 * nasce outra versão e esta fica substituída. Gravada uma vez.
 */
@Entity
@Table(name = "TB_EMISSAO_LAUDO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"pdf", "resultados"})
@EqualsAndHashCode(exclude = {"pdf", "resultados"})
public class EmissaoLaudoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PedidoExame pedido;

    @Column(nullable = false, updatable = false)
    private Integer versao;

    /** XXXX-XXXX-XXXX, sem letras e números que se confundem. */
    @Column(nullable = false, unique = true, length = 14, updatable = false)
    private String codigo;

    /** SHA-256 do PDF, em hexadecimal. */
    @Column(nullable = false, length = 64, updatable = false)
    private String hashPdf;

    @Column(nullable = false, updatable = false)
    private byte[] pdf;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_EMISSAO_LAUDO_EXAME_RESULTADO", joinColumns = @JoinColumn(name = "emissao_id"),
            inverseJoinColumns = @JoinColumn(name = "resultado_id"))
    /** Lista, não conjunto: o hashCode do resultado passa pelo item e volta ao resultado. */
    private List<ResultadoExame> resultados = new ArrayList<>();

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant emitidoEm;

    @Column(updatable = false)
    private String emitidoPorCpf;
}
