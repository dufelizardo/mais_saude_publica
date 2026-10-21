package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoBuscaAtiva;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Uma ação programática (ADR-0132). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcaoProgramaticaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID programaId;
    private String programaCodigo;
    private String programaNome;
    private boolean sensivel;
    private TipoAcaoProgramatica tipo;
    private boolean coletiva;
    private UUID acompanhamentoId;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID unidadeId;
    private String unidadeNome;
    private UUID equipeId;
    private String equipeNome;
    private String local;
    private String descricao;
    private String responsavelMatricula;
    private String responsavelNome;
    private LocalDate dataPrevista;
    private SituacaoAcaoProgramatica situacao;
    /** Pendente com a data prevista passada. */
    private boolean vencida;
    private LocalDate realizadaEm;
    private UUID atendimentoId;
    private UUID agendamentoId;
    private LocalDateTime agendamentoDataHora;
    /** Pendente com o atendimento da marcação já concluído: falta o profissional confirmar. */
    private boolean prontaParaConfirmar;
    private UUID atendimentoSugeridoId;
    private String motivo;
    private ResultadoBuscaAtiva resultadoBusca;
    /** O que a ação sugere a seguir (ex.: encerrar o acompanhamento de quem mudou de endereço). */
    private String sugestao;
    private String observacao;
    private List<Participante> participantes;
    private Instant criadaEm;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Participante implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID pacienteId;
        private String nome;
    }
}
