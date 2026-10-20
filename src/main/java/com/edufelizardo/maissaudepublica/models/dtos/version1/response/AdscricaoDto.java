package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.OrigemAdscricao;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Respostas da adscrição e da cobertura (ADR-0128). */
public final class AdscricaoDto {

    private AdscricaoDto() {
    }

    /** Uma adscrição (vigente ou do histórico). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Item implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private UUID equipeId;
        private String equipeNome;
        private TipoEquipe equipeTipo;
        private UUID unidadeId;
        private String unidadeNome;
        private String microareaCodigo;
        private OrigemAdscricao origem;
        private String motivo;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }

    /** A equipe de referência do paciente, o domicílio e o ACS da microárea, e o histórico. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class DoPaciente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID pacienteId;
        private Item atual;
        private List<Item> historico;
        private /** Onde mora, pelo cadastro territorial; nulo sem família. */
        String domicilio;
        private UUID areaId;
        private String areaCodigo;
        private String microareaCodigo;
        private String acsNome;
    }

    /** Uma microárea na cobertura: cadastro e população de referência. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class MicroareaCobertura implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String codigo;
        private Integer populacaoReferencia;
        private int pessoas;
        private int familias;
        private int imoveis;
        private String acsNome;
        private /** Pessoas cadastradas sobre a população de referência, em %; nulo sem a referência. */
        Integer cobertura;
    }

    /** A cobertura de uma equipe: pessoas adscritas, famílias, imóveis e as microáreas da área. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Cobertura implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID equipeId;
        private String equipeNome;
        private UUID areaId;
        private String areaCodigo;
        private int pessoasAdscritas;
        private int adscritasManual;
        private int familias;
        private int imoveis;
        private int microareasSemCadastro;
        private List<MicroareaCobertura> microareas;
    }

    /** Um paciente adscrito que cabe no público do programa e não está inscrito nele. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Elegivel implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID pacienteId;
        private String nome;
        private Integer idade;
        private Sexo sexo;
        private String microareaCodigo;
        private String acsNome;
    }

    /** Os elegíveis de um programa entre os adscritos de uma equipe (ou microárea). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Elegiveis implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID programaId;
        private UUID equipeId;
        private UUID microareaId;
        private List<String> criteriosAvaliados;
        private /** Critérios clínicos que a lista ainda não avalia (gestação, condição). */
        List<String> criteriosNaoAvaliados;
        private int semDataNascimento;
        private List<Elegivel> elegiveis;
    }
}
