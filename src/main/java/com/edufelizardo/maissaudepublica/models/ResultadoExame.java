package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.InterpretacaoResultado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Um resultado registrado para um exame do pedido (ADR-0093). Nunca é editado: registrar de novo antes da liberação,
 * ou retificar depois dela, grava outro resultado; o item aponta para o atual, e a retificação aponta para o que
 * corrigiu. A faixa de referência e a unidade são copiadas do catálogo no registro.
 */
@Entity
@Table(name = "TB_RESULTADO_EXAME")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ResultadoExame implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private ItemPedidoExame item;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal valorNumerico;

    @Column(length = 2000, updatable = false)
    private String valorTexto;

    @Column(length = 30, updatable = false)
    private String unidadeMedida;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal referenciaMinima;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal referenciaMaxima;

    @Column(length = 200, updatable = false)
    private String referenciaTexto;

    /** Os valores de pânico da faixa usada, copiados no registro (ADR-0180). */
    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal criticoMinimo;

    @Column(precision = 14, scale = 4, updatable = false)
    private BigDecimal criticoMaximo;

    /** Qual faixa valeu, por exemplo "Feminino, 12 a 59 anos" ou "Faixa geral" (ADR-0180). */
    @Column(length = 200, updatable = false)
    private String faixaDescricao;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private InterpretacaoResultado interpretacao;

    @Column(length = 1000, updatable = false)
    private String observacao;

    /** Laudo de imagem (ADR-0182): técnica, achados e conclusão; a conclusão também vai em valorTexto. */
    @Column(length = 1000, updatable = false)
    private String laudoTecnica;

    @Column(length = 4000, updatable = false)
    private String laudoAchados;

    @Column(length = 2000, updatable = false)
    private String laudoConclusao;

    /** Link para as imagens (visualizador do PACS), só https; o PACS é integração do domínio 20. */
    @Column(length = 500, updatable = false)
    private String imagemUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisado_por_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private Profissional analisadoPor;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retificacao_de_id", referencedColumnName = "uuid", updatable = false)
    private ResultadoExame retificacaoDe;

    @Column(length = 1000, updatable = false)
    private String motivoRetificacao;

    /** Liberação: quem responde tecnicamente pelo resultado. Gravada uma vez. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liberado_por_id", referencedColumnName = "uuid")
    private Profissional liberadoPor;

    private Instant liberadoEm;

    /**
     * Assinatura eletrônica interna (ADR-0183): o CPF de quem liberou autenticado e o SHA-256 do conteúdo assinado. Gravados na
     * liberação, no laudo, na retificação e no adendo.
     */
    private String assinadoPorCpf;

    @Column(length = 64)
    private String hashConteudo;

    /** Ciência do solicitante (ADR-0181): o resultado retificado é outro, então a ciência volta a ficar pendente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciencia_por_id", referencedColumnName = "uuid")
    private Profissional cienciaPor;

    private Instant cienciaEm;
}
