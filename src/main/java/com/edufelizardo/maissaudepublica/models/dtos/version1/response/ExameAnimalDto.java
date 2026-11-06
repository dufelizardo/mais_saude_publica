package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoLeishmanioseCanina;
import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoExameAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExameAnimal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Um exame de animal (ADR-0163). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ExameAnimalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID animalId;
    private EspecieAnimal especie;
    private String animalNome;
    private String endereco;
    private UUID observacaoId;
    private TipoExameAnimal tipo;
    private LocalDate dataColeta;
    private ResultadoExameAnimal resultado;
    private Instant resultadoEm;
    private DesfechoLeishmanioseCanina desfechoLeishmaniose;
    private String observacao;
}
