package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** O estoque da sala de vacina (ADR-0148): saldo por vacina, lotes, frascos abertos e perdas do mês. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstoqueSalaVacinaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String unidadeNome;
    private boolean temSalaDeVacina;
    private List<Saldo> porVacina;
    private List<LoteImunobiologicoDto> lotes;
    private List<FrascoAbertoDto> frascos;
    private long aplicadasMes;
    private long perdaTecnicaMes;
    private long perdaFisicaMes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Saldo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID imunobiologicoId;
        private String sigla;
        private String nome;
        /** Só dos lotes aplicáveis. */
        private long doses;
        private int lotes;
    }
}
