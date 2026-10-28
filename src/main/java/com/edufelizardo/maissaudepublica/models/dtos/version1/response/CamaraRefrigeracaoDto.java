package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Câmara de refrigeração com a última leitura (ADR-0148). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CamaraRefrigeracaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeId;
    private String nome;
    private BigDecimal temperaturaMinima;
    private BigDecimal temperaturaMaxima;
    private boolean ativa;
    private LeituraTemperaturaDto ultimaLeitura;
    private long lotesSobSuspeita;
}
