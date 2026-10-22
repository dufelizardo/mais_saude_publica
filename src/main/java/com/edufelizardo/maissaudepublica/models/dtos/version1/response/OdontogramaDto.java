package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O odontograma do paciente numa data (ADR-0135): a projeção dos registros até ela, dente a dente. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class OdontogramaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID pacienteId;
    private LocalDate data;
    /** Os dentes com algum registro até a data. */
    private List<Dente> dentes;
    /** Registros até a data, inclusive os anulados (para o histórico). */
    private int registros;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Dente implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private int dente;
        /** A condição do dente inteiro (o último registro sem face), se houver. */
        private RegistroOdontogramaDto condicaoDente;
        /** As faces registradas depois da condição do dente inteiro. */
        private List<Face> faces;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Face implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private FaceDente face;
        private RegistroOdontogramaDto registro;
    }
}
