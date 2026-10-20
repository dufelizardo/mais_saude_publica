package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAcolhimentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoInicialSaudeMental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Acolhimento em saúde mental (ADR-0120). Não é a triagem de enfermagem (ADR-0047), que classifica risco clínico: aqui
 * ficam o motivo da procura, a necessidade imediata, a vulnerabilidade, o risco percebido e o desfecho. Só inclusão; o
 * atendimento é opcional, porque o acolhimento pode acontecer fora de um atendimento aberto.
 */
@Entity
@Table(name = "TB_ACOLHIMENTO_SAUDE_MENTAL", indexes = {
        @Index(name = "ix_acolhimento_saude_mental_paciente", columnList = "paciente_id, realizado_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcolhimentoSaudeMental implements Serializable {
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

    /** Quem acolheu, quando o usuário logado é um profissional cadastrado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", updatable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", updatable = false)
    private Atendimento atendimento;

    /** O caso aberto ou reaproveitado pelo desfecho; nulo nos desfechos que não acompanham. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acompanhamento_id", referencedColumnName = "uuid", updatable = false)
    private AcompanhamentoSaudeMental acompanhamento;

    @Column(nullable = false, updatable = false)
    private Instant realizadoEm;

    @Column(nullable = false, length = 2000, updatable = false)
    private String motivoProcura;

    @Column(length = 1000, updatable = false)
    private String necessidadeImediata;

    @Column(length = 1000, updatable = false)
    private String vulnerabilidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private RiscoInicialSaudeMental riscoInicial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DesfechoAcolhimentoSaudeMental desfecho;

    /** No encaminhamento: para onde. Texto livre até a referência pela Regulação (SM4). */
    @Column(length = 500, updatable = false)
    private String encaminhamentoDestino;

    @Column(updatable = false)
    private String registradoPorCpf;
}
