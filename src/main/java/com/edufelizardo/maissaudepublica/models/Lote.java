package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Lote de um {@link Medicamento} em estoque em uma {@link UnidadeDeSaude} — segunda entidade do
 * domínio Farmácia (ver MAPA-DE-DOMINIOS.md #9, ADR-0050). Duas FKs diretas por uuid (mesmo padrão
 * de {@code Atendimento.unidade}, ADR-0041): a mesma remessa de um medicamento pode estar
 * fisicamente em unidades diferentes, cada uma com sua própria validade/quantidade.
 * {@code quantidade} é um contador simples, editável via PATCH — não uma soma calculada a partir de
 * um ledger de movimentações (que ainda não existe, ver ADR-0050). Sem campo de status: vencimento
 * e esgotamento são derivados de {@code validade}/{@code quantidade}, não armazenados.
 */
@Entity
@Table(name = "TB_LOTE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Lote implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", referencedColumnName = "uuid", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false)
    private UnidadeDeSaude unidade;

    /**
     * Código impresso na embalagem pelo fabricante, usado pra rastreabilidade em caso de recall —
     * diferente do {@code uuid} interno.
     */
    @NotBlank
    private String numeroLote;

    @NotNull
    private LocalDate validade;

    @NotNull
    @PositiveOrZero
    private Integer quantidade;

    public Lote(Medicamento medicamento, UnidadeDeSaude unidade, String numeroLote, LocalDate validade,
                Integer quantidade) {
        this.medicamento = medicamento;
        this.unidade = unidade;
        this.numeroLote = numeroLote;
        this.validade = validade;
        this.quantidade = quantidade;
    }
}
