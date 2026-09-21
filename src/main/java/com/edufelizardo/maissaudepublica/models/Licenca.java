package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.ResponsavelPagamentoLicenca;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLicenca;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Subtipo de {@link Afastamento} com regras legais próprias — maternidade, paternidade, doença,
 * acidente de trabalho, etc. (ver docs/rh/MODELO-RH.md, seção 5). Ligada 1:1 a um
 * {@code Afastamento} já existente (coluna de join única garante o 1:1) — mantém
 * {@code Afastamento} genérico pros casos (férias, licença pessoal) que não precisam dessa
 * camada extra.
 */
@Entity
@Table(name = "TB_LICENCA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Licenca implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "afastamento_id", referencedColumnName = "uuid", nullable = false, unique = true)
    private Afastamento afastamento;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TipoLicenca tipoLegal;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ResponsavelPagamentoLicenca responsavelPagamento;

    private String documentoUrl;

    public Licenca(Afastamento afastamento, TipoLicenca tipoLegal, ResponsavelPagamentoLicenca responsavelPagamento, String documentoUrl) {
        this.afastamento = afastamento;
        this.tipoLegal = tipoLegal;
        this.responsavelPagamento = responsavelPagamento;
        this.documentoUrl = documentoUrl;
    }
}
