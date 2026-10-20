package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Um caso em saúde mental (ADR-0120). A situação vem do acompanhamento do programa Saúde Mental. Sem
 * {@code conteudoLiberado}, ficam só a existência do caso e a referência (para saber a quem procurar); acolhimentos e
 * avaliações vão vazios, como o PTS, as intervenções e a rede de apoio da SM2 (ADR-0121). Na lista, as coleções também
 * vão vazias.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcompanhamentoSaudeMentalDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private Integer pacienteIdade;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID acompanhamentoProgramaticoId;
    private SituacaoAcompanhamento situacao;
    private LocalDate inicio;
    private LocalDate fim;
    private MotivoSaidaAcompanhamento motivoSaida;
    private String observacaoSaida;
    private UUID equipeId;
    private String equipeNome;
    private String tecnicoMatricula;
    private String tecnicoNome;
    private Instant ultimaAvaliacaoEm;
    private boolean conteudoLiberado;
    private AcessoProntuarioDto acesso;
    private List<Referencia> referencias;
    private List<AcolhimentoSaudeMentalDto> acolhimentos;
    private List<AvaliacaoSaudeMentalDto> avaliacoes;
    /** Versão vigente do PTS (ADR-0121); nula sem PTS. */
    private Integer ptsVersao;
    private LocalDate ptsRevisarEm;
    private boolean ptsRevisaoVencida;
    /** Faltas seguidas nas intervenções mais recentes: sugere marcar faltoso. */
    private int faltasSeguidas;
    /** As versões do PTS, a vigente primeiro; só com conteúdo liberado. */
    private List<ProjetoTerapeuticoDto> pts;
    private List<IntervencaoSaudeMentalDto> intervencoes;
    private List<PessoaRedeApoioDto> redeApoio;
    /** Rede de atenção (ADR-0122): encaminhamentos pela Regulação, cuidado compartilhado e transferências. */
    private List<EncaminhamentoSaudeMentalDto> encaminhamentos;
    private List<CompartilhamentoCuidadoDto> compartilhamentos;
    private List<TransferenciaCuidadoDto> transferencias;
    /** O caso de onde este veio, por transferência do cuidado. */
    private UUID casoAnteriorId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Referencia implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID equipeId;
        private String equipeNome;
        private String tecnicoMatricula;
        private String tecnicoNome;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivo;
    }
}
