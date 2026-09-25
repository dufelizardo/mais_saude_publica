package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.NecessidadeDePessoal;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NecessidadeDePessoalResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeUuid;
    private String unidadeNome;
    private UUID setorUuid;
    private String setorNome;
    private UUID cargoUuid;
    private String cargoNome;
    private Integer quantidade;
    private Integer jornadaSemanalHoras;
    private String competenciasNecessarias;
    private String justificativa;
    private LocalDate dataRegistro;
    private UUID vagaAssociadaUuid;

    public static NecessidadeDePessoalResponseDto fromNecessidadeDePessoal(NecessidadeDePessoal necessidade) {
        UUID setorUuid = null;
        String setorNome = null;
        if (necessidade.getSetor() != null) {
            setorUuid = necessidade.getSetor().getUuid();
            setorNome = necessidade.getSetor().getNome();
        }
        UUID vagaAssociadaUuid = necessidade.getVagaAssociada() != null ? necessidade.getVagaAssociada().getUuid() : null;

        return new NecessidadeDePessoalResponseDto(
                necessidade.getUuid(),
                necessidade.getUnidade().getUuid(),
                necessidade.getUnidade().getNome(),
                setorUuid,
                setorNome,
                necessidade.getCargo().getUuid(),
                necessidade.getCargo().getNome(),
                necessidade.getQuantidade(),
                necessidade.getJornadaSemanalHoras(),
                necessidade.getCompetenciasNecessarias(),
                necessidade.getJustificativa(),
                necessidade.getDataRegistro(),
                vagaAssociadaUuid
        );
    }
}
