package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/** Um item do catálogo geral de procedimentos (ADR-0136). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProcedimentoCatalogoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String codigo;
    private String nome;
    private UUID especialidadeId;
    private String especialidadeNome;
    private boolean odontologico;
    private String sigtap;
    private AlcanceProcedimento alcance;
    private UUID condicaoResultanteId;
    private String condicaoResultanteNome;
    private boolean exigeDentista;
    private boolean ativo;
}
