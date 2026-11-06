package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Os percursos em curso do escopo, atrasados primeiro, e as sugestões de abrir a linha para quem está no programa dela (ADR-0152). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PainelLinhasCuidadoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private long emCurso;
    private long atrasados;
    private List<PercursoLinhaDto> percursos;
    private List<Sugestao> sugestoes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Sugestao implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID pacienteId;
        private String pacienteNome;
        private UUID linhaId;
        private String linhaNome;
        private UUID acompanhamentoId;
        private UUID unidadeId;
        private String unidadeNome;
        private UUID equipeId;
        private LocalDate inscritoDesde;
    }
}
