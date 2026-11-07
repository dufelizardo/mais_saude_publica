package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DonoDocumento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Documento (ADR-0172): reaproveitável por qualquer domínio, pelo dono. Nada se apaga: o documento errado é retirado, com o motivo. */
@Entity
@Table(name = "TB_DOCUMENTO", indexes = @Index(name = "ix_documento_dono", columnList = "dono_tipo, dono_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Documento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "dono_tipo", nullable = false, length = 20, updatable = false)
    private DonoDocumento donoTipo;

    @Column(name = "dono_id", nullable = false, updatable = false)
    private UUID donoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoDocumento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false)
    private LocalDate dataDocumento;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    private LocalDateTime retiradoEm;

    @Column(length = 500)
    private String motivoRetirada;

    private String retiradoPorCpf;

    public boolean retirado() {
        return retiradoEm != null;
    }
}
