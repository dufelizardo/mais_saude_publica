package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Auto de infração que abre o processo sanitário (ADR-0200): as irregularidades abertas do estabelecimento, o dispositivo legal e os fiscais (com login, o logado entre eles). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AutoInfracaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o estabelecimento.")
    private UUID estabelecimentoId;
    private UUID inspecaoId;
    @NotEmpty(message = "Informe ao menos uma irregularidade.")
    private List<UUID> irregularidades;
    @NotBlank(message = "Informe o dispositivo legal infringido.")
    @Size(max = 500, message = "O dispositivo legal vai até 500 caracteres.")
    private String dispositivoLegal;
    @NotBlank(message = "Descreva a infração.")
    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;
    /** Nunca abaixo da maior gravidade das irregularidades; pode ser agravada. */
    private GravidadeIrregularidade gravidade;
    private LocalDate dataLavratura;
    @NotEmpty(message = "Informe ao menos um fiscal.")
    private List<@NotBlank(message = "Informe a matrícula do fiscal.") String> fiscais;
}
