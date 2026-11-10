package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Uma linha do censo nominal (ADR-0185): quem estava internado às 24h, em qual leito e há quantos dias. */
@Entity
@Table(name = "TB_CENSO_DIARIO_PACIENTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CensoDiarioPaciente implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "censo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private CensoDiario censo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Internacao internacao;

    @Column(nullable = false, length = 300, updatable = false)
    private String pacienteNome;

    @Column(nullable = false, length = 60, updatable = false)
    private String leitoIdentificacao;

    @Column(nullable = false, length = 200, updatable = false)
    private String setorNome;

    @Column(nullable = false, updatable = false)
    private Integer diasInternado;
}
