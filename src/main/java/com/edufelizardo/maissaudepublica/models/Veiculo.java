package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Veículo da frota (ADR-0178): placa e prefixo únicos, lotado numa unidade, com a situação atual (o histórico fica em {@link SituacaoVeiculoRegistro}). */
@Entity
@Table(name = "TB_VEICULO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Veiculo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** Sem hífen, maiúsculas: padrão antigo (ABC1234) ou Mercosul (ABC1D23). */
    @Column(nullable = false, unique = true, length = 7)
    private String placa;

    /** Como a central chama a viatura: USA-01, USB-03. */
    @Column(nullable = false, unique = true, length = 20)
    private String prefixo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoVeiculo tipo;

    @Column(length = 80)
    private String modelo;

    private Integer ano;

    private Integer lugares;

    @Column(nullable = false)
    private boolean maca;

    @Column(nullable = false)
    private boolean cadeiraDeRodas;

    private Integer quilometragem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoVeiculo situacao;

    @Column(length = 500)
    private String observacao;
}
