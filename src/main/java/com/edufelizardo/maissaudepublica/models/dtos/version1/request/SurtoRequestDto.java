package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoSurto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSurto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Abertura e edição do surto (ADR-0131). Na edição, o agravo e a unidade não mudam. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SurtoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o agravo.")
    private UUID agravoId;

    @NotNull(message = "Informe a unidade responsável.")
    private UUID unidadeId;

    @NotBlank(message = "Informe o nome do surto.")
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @NotNull(message = "Informe se o surto é comunitário ou institucional.")
    private TipoSurto tipo;

    private TipoInstituicaoSurto tipoInstituicao;

    @Size(max = 200, message = "O nome do estabelecimento vai até 200 caracteres.")
    private String nomeInstituicao;

    @Size(max = 500, message = "O endereço vai até 500 caracteres.")
    private String enderecoInstituicao;

    /** As microáreas afetadas. */
    private List<UUID> microareasIds;

    @Size(max = 2000, message = "A definição de caso vai até 2000 caracteres.")
    private String definicaoCaso;

    @NotNull(message = "Informe o início do surto.")
    private LocalDate inicio;

    private LocalDate fim;

    /** Na abertura: os casos que já entram no surto. */
    private List<UUID> notificacoesIds;
}
