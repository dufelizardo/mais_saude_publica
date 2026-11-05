package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoObservacaoAnimal;
import com.edufelizardo.maissaudepublica.models.enuns.EspecieAnimal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Uma observação de animal agressor (ADR-0163); vencida é a aberta depois do fim previsto. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ObservacaoAnimalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID animalId;
    private EspecieAnimal especie;
    private String animalNome;
    private String tutorNome;
    private String tutorTelefone;
    private String endereco;
    private UUID notificacaoId;
    private LocalDate dataAgressao;
    private LocalDate inicio;
    private LocalDate fimPrevisto;
    private DesfechoObservacaoAnimal desfecho;
    private LocalDate desfechoEm;
    private String observacao;
    private boolean vencida;
}
