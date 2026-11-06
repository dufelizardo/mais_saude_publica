package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A cadeia de frio da sala no dia (ADR-0148): câmaras, leituras, turnos sem leitura e lotes sob suspeita. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CadeiaFrioDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private LocalDate data;
    private List<CamaraRefrigeracaoDto> camaras;
    private List<LeituraTemperaturaDto> leituras;
    /** "Câmara 1: falta a leitura da manhã". */
    private List<String> pendencias;
    private List<LoteImunobiologicoDto> lotesSobSuspeita;
}
