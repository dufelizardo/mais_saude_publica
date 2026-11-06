package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TurnoLeituraTemperatura;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Leitura de temperatura (ADR-0148); na excursão, quantos lotes ficaram sob suspeita. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LeituraTemperaturaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID camaraId;
    private String camaraNome;
    private LocalDateTime lidaEm;
    private TurnoLeituraTemperatura turno;
    private BigDecimal temperaturaAtual;
    private BigDecimal temperaturaMinima;
    private BigDecimal temperaturaMaxima;
    private boolean excursao;
    private String profissionalNome;
    private String observacao;
    private int lotesPostosSobSuspeita;
}
