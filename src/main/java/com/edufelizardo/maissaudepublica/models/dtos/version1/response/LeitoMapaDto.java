package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Um leito no mapa (ADR-0098), com o paciente quando ocupado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LeitoMapaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID setorId;
    private String setorNome;
    private String identificacao;
    private TipoLeito tipo;
    private SexoLeito sexo;
    private SituacaoLeito situacao;
    private String motivoBloqueio;
    private boolean ativo;
    private UUID internacaoId;
    private UUID pacienteId;
    private String pacienteNome;
    private Sexo pacienteSexo;
    private Instant admitidaEm;
    private Long diasInternado;
    private LocalDate previsaoAlta;
    private String medicoResponsavelNome;


    public static LeitoMapaDto fromLeito(Leito l, Internacao i) {
        return new LeitoMapaDto(l.getUuid(), l.getUnidade().getUuid(), l.getUnidade().getNome(), l.getSetor().getUuid(),
                l.getSetor().getNome(), l.getIdentificacao(), l.getTipo(), l.getSexo(), l.getSituacao(), l.getMotivoBloqueio(),
                l.isAtivo(), i != null ? i.getUuid() : null, i != null ? i.getPaciente().getUuid() : null,
                i != null ? i.getPaciente().getNome() : null, i != null ? i.getPaciente().getSexo() : null,
                i != null ? i.getAdmitidaEm() : null,
                i != null ? Duration.between(i.getAdmitidaEm(), Instant.now()).toDays() : null,
                i != null ? i.getPrevisaoAlta() : null, i != null ? i.getMedicoResponsavel().getNome() : null);
    }
}
