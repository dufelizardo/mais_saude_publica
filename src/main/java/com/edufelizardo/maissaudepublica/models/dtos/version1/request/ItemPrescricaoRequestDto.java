package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Item da prescrição hospitalar (ADR-0187): medicamento, dieta ou cuidado. */
@Data
public class ItemPrescricaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o tipo do item.")
    private TipoItemPrescricao tipo;

    private UUID medicamentoId;

    @Size(max = 100, message = "A dose vai até 100 caracteres.")
    private String dose;

    private ViaAdministracao via;

    private TipoDieta tipoDieta;

    @Size(max = 1000, message = "A descrição vai até 1000 caracteres.")
    private String descricao;

    @Min(value = 1, message = "O intervalo vai de 1 a 48 horas.")
    @Max(value = 48, message = "O intervalo vai de 1 a 48 horas.")
    private Integer intervaloHoras;

    private Instant primeiroHorario;

    private boolean seNecessario;

    @Size(max = 500, message = "A condição vai até 500 caracteres.")
    private String condicao;
}
