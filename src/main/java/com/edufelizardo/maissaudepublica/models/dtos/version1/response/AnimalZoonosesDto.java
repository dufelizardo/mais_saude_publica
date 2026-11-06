package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Um animal de interesse das zoonoses (ADR-0163). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AnimalZoonosesDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private EspecieAnimal especie;
    private String nome;
    private String descricao;
    private String tutorNome;
    private String tutorTelefone;
    private UUID imovelId;
    private String endereco;
    private String microareaCodigo;
    private String areaNome;
    private boolean emObservacao;
    private Instant registradoEm;
}
