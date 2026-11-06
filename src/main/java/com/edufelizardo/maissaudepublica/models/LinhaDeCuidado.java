package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Linha de cuidado (ADR-0152, desenho na ADR-0109): o percurso assistencial que atravessa domínios, como a gestação da
 * captação ao puerpério. Catálogo cadastrável; as etapas são dado. Ligada a programa sensível, é sensível.
 */
@Entity
@Table(name = "TB_LINHA_CUIDADO", uniqueConstraints = @UniqueConstraint(name = "uk_linha_cuidado_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LinhaDeCuidado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, length = 60)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    /** O programa a que a linha se liga (opcional): a sensibilidade e a sugestão de abrir o percurso vêm dele. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programa_id", referencedColumnName = "uuid")
    private Programa programa;

    @Column(nullable = false)
    private boolean ativa;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean isSensivel() {
        return programa != null && programa.isSensivel();
    }
}
