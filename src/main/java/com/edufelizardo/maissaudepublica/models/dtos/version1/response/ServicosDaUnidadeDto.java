package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Os serviços da unidade (ADR-0165): vigentes, encerrados e os setores livres para ligar. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ServicosDaUnidadeDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private List<Servico> servicos;
    private List<Servico> encerrados;
    private List<SetorRef> setoresLivres;

    /** Um serviço, com os setores e os leitos que vêm deles. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Servico implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private UUID tipoId;
        private String tipoCodigo;
        private String tipoNome;
        private String nome;
        private String responsavelMatricula;
        private String responsavelNome;
        private boolean vinteQuatroHoras;
        private String horario;
        private List<SetorRef> setores;
        private int leitos;
        private int leitosLivres;
        private int leitosOcupados;
        private LocalDate inicio;
        private LocalDate fim;
        private String motivoFim;
    }

    /** Um setor da unidade. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class SetorRef implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;
        private String nome;
    }
}
