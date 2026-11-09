package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DecisaoRegulacaoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.LocalRealizacao;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRecursoUrgencia;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Ocorrência de urgência (ADR-0176). Sem {@code clinico}, vem só o andamento: sem queixa, conduta, nome do paciente nem
 * quem chamou (quem só tem URGENCIA.CONSULTAR). Os eventos e o texto deles vêm no detalhe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OcorrenciaUrgenciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String protocolo;
    private UUID centralId;
    private String centralNome;
    private OrigemOcorrenciaUrgencia origem;
    private SituacaoOcorrenciaUrgencia situacao;
    private PrioridadeRegulacao prioridade;
    private boolean clinico;

    private String solicitanteNome;
    private String solicitanteTelefone;
    private UUID unidadeSolicitanteId;
    private String unidadeSolicitanteNome;
    private UUID criseId;
    private UUID pacienteId;
    private String pacienteNome;
    private String descricaoPaciente;
    private LocalRealizacao localTipo;
    private String endereco;
    private String referencia;
    private Double latitude;
    private Double longitude;
    private String queixa;
    private Instant recebidaEm;

    private Instant reguladaEm;
    private String reguladorNome;
    private DecisaoRegulacaoUrgencia decisao;
    private String conduta;
    private TipoRecursoUrgencia tipoRecurso;
    private String recursoIdentificacao;
    private UUID veiculoId;
    private String veiculoPrefixo;
    private UUID destinoId;
    private String destinoNome;
    private boolean vagaZero;
    private String justificativaVagaZero;
    private Instant recebidaNoDestinoEm;
    private DesfechoOcorrenciaUrgencia desfecho;
    private Instant encerradaEm;

    /** O último passo de deslocamento registrado (envio, chegada ao local, saída, chegada ao destino). */
    private TipoEventoUrgencia ultimoDeslocamento;
    /** Tempos de resposta em minutos, a partir dos eventos; nulos enquanto o passo não aconteceu. */
    private Long minutosAteRegulacao;
    private Long minutosAteEnvio;
    private Long minutosEnvioAteLocal;
    private Long minutosTotal;

    /** Só no detalhe. */
    private List<Evento> eventos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Evento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private TipoEventoUrgencia tipo;
        private SituacaoOcorrenciaUrgencia situacaoResultante;
        private PrioridadeRegulacao prioridade;
        private String texto;
        private String profissionalNome;
        private Instant ocorridoEm;
    }
}
