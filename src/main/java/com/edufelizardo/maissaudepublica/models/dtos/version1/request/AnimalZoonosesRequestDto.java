package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Animal de interesse das zoonoses (ADR-0163): o imóvel do Território ou o endereço (um dos dois, 400). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnimalZoonosesRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Informe a espécie.")
    private EspecieAnimal especie;
    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;
    @Size(max = 500, message = "A descrição vai até 500 caracteres.")
    private String descricao;
    @Size(max = 200, message = "O nome do tutor vai até 200 caracteres.")
    private String tutorNome;
    @Size(max = 40, message = "O telefone vai até 40 caracteres.")
    private String tutorTelefone;
    private UUID imovelId;
    @Size(max = 300, message = "O endereço vai até 300 caracteres.")
    private String endereco;
}
