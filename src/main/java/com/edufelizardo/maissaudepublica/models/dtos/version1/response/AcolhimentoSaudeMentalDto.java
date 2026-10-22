package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAcolhimentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoInicialSaudeMental;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Um acolhimento em saúde mental (ADR-0120). Sem {@code conteudoLiberado}, só ficam a data e a unidade: o conteúdo é da
 * equipe de referência, de quem registrou e do acesso justificado. {@code aviso}: o que o registro disparou (caso
 * reaproveitado, risco alto).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcolhimentoSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private UUID unidadeId;
    private String unidadeNome;
    private Instant realizadoEm;
    private boolean conteudoLiberado;
    private String profissionalNome;
    private UUID atendimentoId;
    private String motivoProcura;
    private String necessidadeImediata;
    private String vulnerabilidade;
    private RiscoInicialSaudeMental riscoInicial;
    private DesfechoAcolhimentoSaudeMental desfecho;
    private String encaminhamentoDestino;
    private UUID acompanhamentoId;
    private String aviso;
}
