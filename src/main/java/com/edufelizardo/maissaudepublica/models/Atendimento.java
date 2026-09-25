package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de entrada do paciente na rede — segunda entidade da onda "Operação Assistencial" (ver
 * ADR-0039, MAPA-DE-DOMINIOS.md #4). {@code profissional} referencia {@link Profissional} por FK
 * direta (uuid interno), resolvida a partir da matrícula na fronteira da API — mesmo padrão do
 * {@code Setor.responsavel} (ver ADR-0034). {@code setor} é opcional (nem todo atendimento passa
 * por um setor específico). Sem campo {@code agendamento} ainda: a entidade `Agendamento` é a
 * próxima fatia da mesma onda (ver ADR-0041) — o vínculo opcional será acrescentado quando ela
 * existir, mesmo padrão de "campo chega depois" já usado por {@code Setor.responsavel} (ADR-0030 →
 * ADR-0034).
 */
@Entity
@Table(name = "TB_ATENDIMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Atendimento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", referencedColumnName = "uuid", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", referencedColumnName = "uuid", nullable = true)
    private Setor setor;

    @Enumerated(EnumType.STRING)
    private TipoAtendimento tipo;

    @Enumerated(EnumType.STRING)
    private StatusAtendimento status;

    @NotNull
    private LocalDateTime dataHora;

    public Atendimento(Paciente paciente, Profissional profissional, UnidadeDeSaude unidade, Setor setor,
                        TipoAtendimento tipo, StatusAtendimento status, LocalDateTime dataHora) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.unidade = unidade;
        this.setor = setor;
        this.tipo = tipo;
        this.status = status;
        this.dataHora = dataHora;
    }
}
