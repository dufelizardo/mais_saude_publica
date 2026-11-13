package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoSolicitacaoLeito;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Um passo do pedido de leito (ADR-0184): quem, quando e o texto. Gravado uma vez. */
@Entity
@Table(name = "TB_EVENTO_SOLICITACAO_LEITO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoSolicitacaoLeito implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private SolicitacaoLeito solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEventoSolicitacaoLeito tipo;

    @Column(length = 1000, updatable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
