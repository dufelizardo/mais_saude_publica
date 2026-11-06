package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ReferenciaPrazoEtapa;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEtapaLinhaCuidado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Linha de cuidado com as etapas (ADR-0152); o catálogo não tem dado de paciente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LinhaCuidadoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private String descricao;
    private UUID programaId;
    private String programaNome;
    private boolean sensivel;
    private boolean ativa;
    private long percursosEmCurso;
    private List<Etapa> etapas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Etapa implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private int ordem;
        private String nome;
        private TipoEtapaLinhaCuidado tipo;
        private UUID especialidadeId;
        private String especialidadeNome;
        private UUID exameId;
        private String exameNome;
        private UUID procedimentoId;
        private String procedimentoNome;
        private UUID imunobiologicoId;
        private String imunobiologicoSigla;
        private String cids;
        private int quantidade;
        private Integer prazoDias;
        private ReferenciaPrazoEtapa prazoDesde;
        private boolean obrigatoria;
        private boolean ativa;
        /** Como se cumpre, em palavras ("Vacina dTpa", "Internação com CID O80, O81"). */
        private String criterio;
    }
}
