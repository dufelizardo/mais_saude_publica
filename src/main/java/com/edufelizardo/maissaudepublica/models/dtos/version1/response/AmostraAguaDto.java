package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoAbastecimentoAgua;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma amostra de água (ADR-0164), com a conformidade e a medida. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AmostraAguaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pontoId;
    private String pontoNome;
    private TipoAbastecimentoAgua tipo;
    private LocalDate dataColeta;
    private Boolean coliformesTotais;
    private Boolean ecoli;
    private Double cloroResidual;
    private Double turbidez;
    private Double fluor;
    private boolean conforme;
    private List<String> naoConformidades;
    private String medida;
    private Instant medidaEm;
}
