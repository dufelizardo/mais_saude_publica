package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroAtivacao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Um passo da ativação (ADR-0157): ativado, nível alterado ou desativado, com o nível, se o plano cobre esse nível, a autoridade e a justificativa. Só inclusão. */
@Entity
@Table(name = "TB_REGISTRO_ATIVACAO_PLANO", indexes = @Index(name = "ix_registro_ativacao_plano", columnList = "ativacao_id, registrado_em"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroAtivacaoPlano implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativacao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AtivacaoPlano ativacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoRegistroAtivacao tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nivel_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private NivelEmergencia nivel;

    @Column(nullable = false, updatable = false)
    private boolean nivelCoberto;

    /** Quem ativou ou desativou (ex.: Secretário de Saúde); no nível alterado, nulo. */
    @Column(length = 200, updatable = false)
    private String autoridade;

    @Column(nullable = false, length = 1000, updatable = false)
    private String justificativa;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
