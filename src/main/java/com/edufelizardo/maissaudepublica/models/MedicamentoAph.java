package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Medicamento dado no atendimento pré-hospitalar (ADR-0188), em texto: o estoque da viatura fica fora desta fatia. */
@Entity
@Table(name = "TB_MEDICAMENTO_APH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedicamentoAph implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private FichaAph ficha;

    @Column(nullable = false, length = 200, updatable = false)
    private String nome;

    @Column(nullable = false, length = 100, updatable = false)
    private String dose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ViaAdministracao via;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant administradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional profissional;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
