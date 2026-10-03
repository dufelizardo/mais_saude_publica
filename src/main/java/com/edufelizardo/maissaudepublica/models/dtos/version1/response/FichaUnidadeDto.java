package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TurnoHorarioDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A ficha da unidade (ADR-0101): dados, horário, vínculos, profissionais lotados e histórico de situação. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FichaUnidadeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private RedeUnidadeResumoDto resumo;
    private String cep;
    private String logradouro;
    private String numeroLogradouro;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private Set<String> telefones;
    private String responsavelCpf;
    private String responsavelNome;
    private List<TurnoHorarioDto> turnos;
    /** Horário em texto livre do cadastro antigo, mostrado quando não há horário estruturado. */
    private Map<DayOfWeek, String> horarioFuncionamentoTexto;
    private List<Vinculo> subordinadas;
    private List<Vinculo> setoresDaUnidade;
    private List<Profissional> profissionais;
    private List<Evento> historico;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Vinculo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private UUID uuid;
        private String nome;
        private String tipo;
        private boolean ativo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Profissional implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String matricula;
        private String nome;
        private String cargo;
        private String conselho;
        private Integer jornadaSemanalHoras;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Evento implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private SituacaoOperacional situacaoAnterior;
        private SituacaoOperacional situacao;
        private String motivo;
        private LocalDate previsaoRetorno;
        private Instant ocorridoEm;
        private String registradoPorCpf;
        private String registradoPorNome;
    }
}
