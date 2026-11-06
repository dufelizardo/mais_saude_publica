package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A caderneta de vacinação do paciente (ADR-0147): as doses, o calendário com a situação e as recomendações. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CadernetaVacinacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private String pacienteNome;
    private LocalDate dataNascimento;
    private Integer idadeMeses;
    /** Sem a data de nascimento, não há como calcular. */
    private boolean situacaoDisponivel;
    private boolean emDia;
    private long atrasadas;
    private long proximas;
    private List<DoseCalendarioDto> calendario;
    /** Conforme a situação vacinal (adulto, idoso, gestante, campanhas anuais): não entram nos atrasos. */
    private List<EsquemaVacinalDto> recomendacoes;
    private List<RegistroVacinacaoDto> doses;
    /** As contraindicações por ESAVI e as doses já notificadas na Vigilância (ADR-0150). */
    private List<Contraindicacao> contraindicacoes;
    private List<UUID> dosesNotificadas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Contraindicacao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID imunobiologicoId;
        private String sigla;
        private String nome;
        private LocalDate desde;
        private String motivo;
        private UUID notificacaoId;
    }
}
