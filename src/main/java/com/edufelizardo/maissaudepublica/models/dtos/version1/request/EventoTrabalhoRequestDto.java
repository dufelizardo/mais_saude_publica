package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.RespostaCat;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMercadoTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcidenteTrabalho;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** O bloco do trabalho da notificação (ADR-0207). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventoTrabalhoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Código da CBO, seis dígitos (com ou sem o hífen). */
    @NotBlank(message = "Informe a ocupação (código CBO).")
    private String ocupacaoCbo;

    @NotBlank(message = "Informe a descrição da ocupação.")
    @Size(max = 200, message = "A descrição da ocupação vai até 200 caracteres.")
    private String ocupacaoDescricao;

    @NotNull(message = "Informe a situação no mercado de trabalho.")
    private SituacaoMercadoTrabalho situacaoTrabalho;

    /** CPF ou CNPJ do empregador, conferido. */
    private String empregadorDocumento;

    @Size(max = 200, message = "O nome do empregador vai até 200 caracteres.")
    private String empregadorNome;

    /** CNAE do empregador, sete dígitos (com ou sem a pontuação). */
    private String empregadorCnae;

    @Size(max = 500, message = "O local do evento vai até 500 caracteres.")
    private String localEvento;

    @DecimalMin(value = "-90", message = "A latitude fica entre -90 e 90.")
    @DecimalMax(value = "90", message = "A latitude fica entre -90 e 90.")
    private Double latitude;

    @DecimalMin(value = "-180", message = "A longitude fica entre -180 e 180.")
    @DecimalMax(value = "180", message = "A longitude fica entre -180 e 180.")
    private Double longitude;

    /** Só no acidente; obrigatório no acidente de trabalho grave. */
    private TipoAcidenteTrabalho tipoAcidente;

    @Min(value = 0, message = "As horas depois do início da jornada vão de 0 a 24.")
    @Max(value = 24, message = "As horas depois do início da jornada vão de 0 a 24.")
    private Integer horasAposInicioJornada;

    private RespostaCat cat;

    @Min(value = 0, message = "Os outros trabalhadores atingidos são 0 ou mais.")
    private Integer outrosTrabalhadoresAtingidos;

    private Boolean obito;

    /** O acidente do servidor no RH, do mesmo CPF do paciente. */
    private UUID acidenteTrabalhoId;
}
