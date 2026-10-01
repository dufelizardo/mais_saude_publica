package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAfastamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Um profissional do quadro com a lotação vigente (unidade, cargo, jornada) e o afastamento em curso, quando
 * houver — o que a tela Profissionais mostra em cada cartão (ADR-0072).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class QuadroProfissionalResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ProfissionalResponseDto profissional;
    /** Nula sem lotação vigente. */
    private LotacaoVigente lotacao;
    /** Nulo sem afastamento em curso. */
    private AfastamentoVigente afastamento;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class LotacaoVigente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID unidadeUuid;
        private String unidadeNome;
        private UUID cargoUuid;
        private String cargoNome;
        private Integer jornadaSemanalHoras;
        private LocalDate dataInicio;

        public static LotacaoVigente fromLotacao(Lotacao l) {
            return new LotacaoVigente(l.getUnidade().getUuid(), l.getUnidade().getNome(), l.getCargo().getUuid(),
                    l.getCargo().getNome(), l.getJornadaSemanalHoras(), l.getDataInicio());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class AfastamentoVigente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private TipoAfastamento tipo;
        private LocalDate dataInicio;
        private LocalDate dataFim;

        public static AfastamentoVigente fromAfastamento(Afastamento a) {
            return new AfastamentoVigente(a.getTipo(), a.getDataInicio(), a.getDataFim());
        }
    }
}
