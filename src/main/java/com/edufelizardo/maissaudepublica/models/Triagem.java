package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoRisco;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Triagem realizada durante um {@link Atendimento} — primeira entidade do domínio Enfermagem (ver
 * MAPA-DE-DOMINIOS.md #8, ADR-0047). Mesmo papel estrutural de {@link Consulta}: FK obrigatória a
 * {@code Atendimento}, {@code profissional} resolvido por matrícula na fronteira da API (ADR-0034/
 * ADR-0041). {@code classificacaoRisco} segue o protocolo de Manchester (AZUL/VERDE/AMARELO/
 * LARANJA/VERMELHO), mantido como campo enum — sem entidade {@code ClassificacaoDeRisco} própria
 * (YAGNI, ver ADR-0047).
 */
@Entity
@Table(name = "TB_TRIAGEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Triagem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", referencedColumnName = "uuid", nullable = false)
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", referencedColumnName = "uuid", nullable = false)
    private Profissional profissional;

    @NotNull
    private LocalDateTime dataHora;

    private String pressaoArterial;
    private Double temperatura;
    private Double saturacaoOxigenio;
    private Integer frequenciaCardiaca;
    private Double peso;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ClassificacaoRisco classificacaoRisco;

    private String observacoes;

    public Triagem(Atendimento atendimento, Profissional profissional, LocalDateTime dataHora,
                    String pressaoArterial, Double temperatura, Double saturacaoOxigenio,
                    Integer frequenciaCardiaca, Double peso, ClassificacaoRisco classificacaoRisco,
                    String observacoes) {
        this.atendimento = atendimento;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.pressaoArterial = pressaoArterial;
        this.temperatura = temperatura;
        this.saturacaoOxigenio = saturacaoOxigenio;
        this.frequenciaCardiaca = frequenciaCardiaca;
        this.peso = peso;
        this.classificacaoRisco = classificacaoRisco;
        this.observacoes = observacoes;
    }
}
