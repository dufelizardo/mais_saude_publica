package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoVistoriaVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVistoriaVetorial;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Uma vistoria do controle vetorial (ADR-0162), com os focos. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VistoriaVetorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID imovelId;
    private String endereco;
    private String microareaCodigo;
    private String areaNome;
    private LocalDate data;
    private int cicloAno;
    private int cicloNumero;
    private String agenteMatricula;
    private String agenteNome;
    private TipoVistoriaVetorial tipo;
    private ResultadoVistoriaVetorial resultado;
    private int depositosInspecionados;
    private int depositosA1;
    private int depositosA2;
    private int depositosB;
    private int depositosC;
    private int depositosD1;
    private int depositosD2;
    private int depositosE;
    private int depositosEliminados;
    private int depositosTratados;
    private String larvicida;
    private Double larvicidaGramas;
    private UUID acaoId;
    private String observacao;
    private List<FocoVetorialDto> focos;
    private Instant registradoEm;
}
