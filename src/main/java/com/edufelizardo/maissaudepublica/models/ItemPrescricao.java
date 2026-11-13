package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Item da prescrição hospitalar (ADR-0187): medicamento (dose, via, intervalo ou "se necessário"), dieta ou cuidado. Não é editado;
 * o médico suspende com motivo, e os horários pendentes ficam cancelados.
 */
@Entity
@Table(name = "TB_ITEM_PRESCRICAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ItemPrescricao implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescricao_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private PrescricaoHospitalar prescricao;

    @Column(nullable = false, updatable = false)
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoItemPrescricao tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", referencedColumnName = "uuid", updatable = false)
    private Medicamento medicamento;

    @Column(length = 100, updatable = false)
    private String dose;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private ViaAdministracao via;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private TipoDieta tipoDieta;

    /** O cuidado, ou a observação do medicamento (diluição, infusão) e da dieta (consistência, volume). */
    @Column(length = 1000, updatable = false)
    private String descricao;

    /** Intervalo entre os horários; nulo: dose única (medicamento), refeições (dieta oral) ou contínuo. */
    @Column(updatable = false)
    private Integer intervaloHoras;

    @Column(updatable = false)
    private Instant primeiroHorario;

    @Column(updatable = false)
    private boolean seNecessario;

    @Column(length = 500, updatable = false)
    private String condicao;

    private Instant suspensoEm;

    @Column(length = 1000)
    private String motivoSuspensao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspenso_por_id", referencedColumnName = "uuid")
    private Profissional suspensoPor;
}
