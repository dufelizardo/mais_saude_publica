package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.CanalDenuncia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Denúncia sanitária (ADR-0199): o estabelecimento cadastrado ou descrito (400); anônima sem os dados do denunciante, identificada com o nome (400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DenunciaSanitariaRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe o canal.")
    private CanalDenuncia canal;
    @NotBlank(message = "Descreva a denúncia.")
    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;
    private UUID estabelecimentoId;
    @Size(max = 300, message = "A descrição do estabelecimento vai até 300 caracteres.")
    private String estabelecimentoDescricao;
    private Boolean anonima;
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String denuncianteNome;
    @Size(max = 200, message = "O contato vai até 200 caracteres.")
    private String denuncianteContato;
}
