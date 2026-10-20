package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * O caso longitudinal em saúde mental (ADR-0120, desenho na ADR-0112). Não duplica Programas: aponta para o
 * {@link AcompanhamentoProgramatico} do programa Saúde Mental, de onde vêm a situação (ativo, faltoso) e as contagens. O
 * detalhe clínico (acolhimentos, avaliações e, nas próximas fatias, PTS e crise) fica aqui. Um caso vigente por paciente;
 * voltar ao cuidado abre um registro novo.
 */
@Entity
@Table(name = "TB_ACOMPANHAMENTO_SAUDE_MENTAL", indexes = {
        @Index(name = "ix_acomp_saude_mental_paciente", columnList = "paciente_id, fim"),
        @Index(name = "ix_acomp_saude_mental_unidade", columnList = "unidade_id, fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoSaudeMental implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    /** O acompanhamento do programa Saúde Mental: registrado na abertura ou reaproveitado, se já vigente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_programatico_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private AcompanhamentoProgramatico acompanhamentoProgramatico;

    @Column(nullable = false, updatable = false)
    private LocalDate inicio;

    private LocalDate fim;

    @Enumerated(EnumType.STRING)
    private MotivoSaidaAcompanhamento motivoSaida;

    @Column(length = 500)
    private String observacaoSaida;

    @Column(updatable = false)
    private String registradoPorCpf;

    public boolean vigente() {
        return fim == null;
    }
}
