package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Estabelecimento regulado pela Vigilância Sanitária (ADR-0199). Não é a UnidadeDeSaude: farmácia, restaurante e clínica particular são regulados sem ser da rede; a unidade pública inspecionada liga-se opcionalmente. */
@Entity
@Table(name = "TB_ESTABELECIMENTO_REGULADO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstabelecimentoRegulado implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** CPF (11) ou CNPJ (14), só os dígitos. */
    @Column(nullable = false, unique = true, length = 14)
    private String documento;

    @Column(nullable = false, length = 200)
    private String razaoSocial;

    @Column(length = 200)
    private String nomeFantasia;

    @Column(nullable = false, length = 300)
    private String endereco;

    private Double latitude;

    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "microarea_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Microarea microarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadeDeSaude unidade;

    @Column(length = 30)
    private String telefone;

    @Column(length = 200)
    private String responsavelLegal;

    @Column(length = 200)
    private String responsavelTecnico;

    @Column(length = 50)
    private String registroConselhoRt;

    @Column(nullable = false)
    private boolean ativo;

    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("cnae")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AtividadeEstabelecimento> atividades = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
