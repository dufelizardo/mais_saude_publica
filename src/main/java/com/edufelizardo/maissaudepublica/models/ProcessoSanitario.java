package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.FaseProcessoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.FormaCiencia;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoDecisaoSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoRecursoSanitario;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Processo administrativo sanitário aberto pelo auto de infração (Lei 6.437/1977; ADR-0200): ciência, defesa em 15 dias, decisão, recurso em 15 dias e encerramento. O auto e o processo ficam no mesmo registro. */
@Entity
@Table(name = "TB_PROCESSO_SANITARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcessoSanitario implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EstabelecimentoRegulado estabelecimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspecao_id", referencedColumnName = "uuid", updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InspecaoSanitaria inspecao;

    @Column(nullable = false, length = 500, updatable = false)
    private String dispositivoLegal;

    @Column(nullable = false, length = 2000, updatable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GravidadeIrregularidade gravidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaseProcessoSanitario fase;

    @Column(nullable = false, updatable = false)
    private LocalDate dataLavratura;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_PROCESSO_SANITARIO_FISCAL", joinColumns = @JoinColumn(name = "processo_id"), inverseJoinColumns = @JoinColumn(name = "profissional_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Profissional> fiscais = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant lavradoEm;

    @Column(updatable = false)
    private String lavradoPorCpf;

    @Column(length = 64)
    private String hashAuto;

    private LocalDate cienciaAutoEm;

    @Enumerated(EnumType.STRING)
    @Column
    private FormaCiencia formaCienciaAuto;

    private LocalDate defesaEm;

    @Column(length = 4000)
    private String defesaTexto;

    private Boolean defesaIntempestiva;

    @Enumerated(EnumType.STRING)
    @Column
    private ResultadoDecisaoSanitaria decisaoResultado;

    @Column(length = 4000)
    private String decisaoFundamentacao;

    @Column(length = 500)
    private String atenuantes;

    @Column(length = 500)
    private String agravantes;

    private LocalDate dataDecisao;

    private String decididoPorCpf;

    private LocalDate cienciaDecisaoEm;

    @Enumerated(EnumType.STRING)
    @Column
    private FormaCiencia formaCienciaDecisao;

    private LocalDate recursoEm;

    @Column(length = 4000)
    private String recursoTexto;

    @Enumerated(EnumType.STRING)
    @Column
    private ResultadoRecursoSanitario recursoResultado;

    @Column(length = 4000)
    private String recursoFundamentacao;

    private LocalDate dataJulgamentoRecurso;

    private String recursoJulgadoPorCpf;

    private LocalDate encerradoEm;
}
