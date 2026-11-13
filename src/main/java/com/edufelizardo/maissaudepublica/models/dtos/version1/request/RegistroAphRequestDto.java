package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registros da ficha do APH (ADR-0188): uma série de sinais vitais (com faixas válidas), um medicamento dado, a finalização ou um adendo
 * (texto). Sempre com a matrícula de quem registra.
 */
@Data
public class RegistroAphRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe a matrícula.")
    private String profissionalMatricula;

    private Instant em;

    @Min(value = 0, message = "A pressão sistólica vai de 0 a 300.")
    @Max(value = 300, message = "A pressão sistólica vai de 0 a 300.")
    private Integer paSistolica;

    @Min(value = 0, message = "A pressão diastólica vai de 0 a 200.")
    @Max(value = 200, message = "A pressão diastólica vai de 0 a 200.")
    private Integer paDiastolica;

    @Min(value = 0, message = "A frequência cardíaca vai de 0 a 300.")
    @Max(value = 300, message = "A frequência cardíaca vai de 0 a 300.")
    private Integer frequenciaCardiaca;

    @Min(value = 0, message = "A frequência respiratória vai de 0 a 80.")
    @Max(value = 80, message = "A frequência respiratória vai de 0 a 80.")
    private Integer frequenciaRespiratoria;

    @Min(value = 0, message = "A saturação vai de 0 a 100.")
    @Max(value = 100, message = "A saturação vai de 0 a 100.")
    private Integer saturacao;

    @DecimalMin(value = "25.0", message = "A temperatura vai de 25 a 45 °C.")
    @DecimalMax(value = "45.0", message = "A temperatura vai de 25 a 45 °C.")
    private BigDecimal temperatura;

    @Min(value = 0, message = "A glicemia vai de 0 a 1500.")
    @Max(value = 1500, message = "A glicemia vai de 0 a 1500.")
    private Integer glicemia;

    @Min(value = 0, message = "A dor vai de 0 a 10.")
    @Max(value = 10, message = "A dor vai de 0 a 10.")
    private Integer dor;

    @Min(value = 3, message = "O Glasgow vai de 3 a 15.")
    @Max(value = 15, message = "O Glasgow vai de 3 a 15.")
    private Integer glasgow;

    @Size(max = 200, message = "O nome do medicamento vai até 200 caracteres.")
    private String nome;

    @Size(max = 100, message = "A dose vai até 100 caracteres.")
    private String dose;

    private ViaAdministracao via;

    @Size(max = 2000, message = "O texto vai até 2000 caracteres.")
    private String texto;
}
