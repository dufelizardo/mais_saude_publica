package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.AbastecimentoAgua;
import com.edufelizardo.maissaudepublica.models.enuns.DestinoLixo;
import com.edufelizardo.maissaudepublica.models.enuns.EscoamentoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.LocalizacaoDomicilio;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMoradia;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Domicílio (ADR-0127): a moradia dentro do imóvel ("apto 12"), com as condições da ficha domiciliar do e-SUS. */
@Entity
@Table(name = "TB_DOMICILIO", indexes = @Index(name = "ix_domicilio_imovel", columnList = "imovel_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Domicilio implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imovel_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Imovel imovel;

    /** Como se distingue no imóvel: "apto 12", "casa dos fundos". Sem valor, o único. */
    @Column(length = 60)
    private String identificacao;

    @Enumerated(EnumType.STRING)
    private SituacaoMoradia situacao;

    @Enumerated(EnumType.STRING)
    private LocalizacaoDomicilio localizacao;

    private Integer comodos;

    @Enumerated(EnumType.STRING)
    private AbastecimentoAgua abastecimentoAgua;

    @Enumerated(EnumType.STRING)
    private EscoamentoSanitario escoamentoSanitario;

    @Enumerated(EnumType.STRING)
    private DestinoLixo destinoLixo;

    private Boolean energiaEletrica;

    @Column(nullable = false)
    private boolean ativo;
}
