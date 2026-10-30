package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * O painel da epidemia (ADR-0161), lido da Vigilância: as notificações do agravo desde 4 semanas antes do início, por
 * semana epidemiológica dos primeiros sintomas; na área afetada, quando há microáreas. Só números, sem dado de paciente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PainelEpidemiaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID agravoId;
    private String agravoNome;
    /** O agravo veio do surto de origem (true) ou foi escolhido na emergência. */
    private boolean doSurto;
    private LocalDate desde;
    private LocalDate ate;
    private boolean naAreaAfetada;
    private int notificados;
    private int confirmados;
    private int descartados;
    private int emInvestigacao;
    private int obitos;
    private int semanaAtual;
    private int semanaAnterior;
    private List<MapaVigilanciaDto.Semana> curva;
}
