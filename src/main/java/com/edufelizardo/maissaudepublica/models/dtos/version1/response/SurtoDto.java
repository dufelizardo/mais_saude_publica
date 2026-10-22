package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSurto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoSurto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSurto;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Um surto (ADR-0131); a lista traz o resumo, e o detalhe, os casos, as medidas, a curva e a linha do tempo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SurtoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID agravoId;
    private String agravoCodigo;
    private String agravoNome;
    private boolean sensivel;
    private UUID unidadeId;
    private String unidadeNome;
    private String nome;
    private TipoSurto tipo;
    private TipoInstituicaoSurto tipoInstituicao;
    private String nomeInstituicao;
    private String enderecoInstituicao;
    private String definicaoCaso;
    private LocalDate inicio;
    private LocalDate fim;
    private SituacaoSurto situacao;
    private String conclusao;
    private Instant encerradoEm;
    private Instant abertoEm;
    private int casos;
    private int confirmados;
    private int obitos;
    private int medidasAbertas;
    private int medidasVencidas;
    private List<Microarea> microareas;
    private List<Caso> listaCasos;
    private List<MedidaControleDto> medidas;
    private List<MapaVigilanciaDto.Semana> curva;
    private List<EventoLinhaDoTempoDto> linhaDoTempo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Microarea implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID microareaId;
        private String codigo;
        private String areaCodigo;
        private String geometria;
        private int casos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Caso implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID notificacaoId;
        private String pacienteNome;
        private String unidadeNome;
        private LocalDate dataPrimeirosSintomas;
        private LocalDate dataNotificacao;
        private SituacaoNotificacao situacao;
        private ClassificacaoCasoEpidemiologico classificacao;
        private EvolucaoCaso evolucao;
        private String microareaCodigo;
        private Instant incluidoEm;
    }
}
