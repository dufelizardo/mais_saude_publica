package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.ViaVacina;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Imunobiológico do catálogo (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ImunobiologicoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String sigla;
    private TipoImunobiologico tipo;
    private ViaVacina via;
    private BigDecimal volumeDoseMl;
    private int dosesPorFrasco;
    private Integer validadeAposAbertoHoras;
    private String codigoSipni;
    private boolean ativo;
}
