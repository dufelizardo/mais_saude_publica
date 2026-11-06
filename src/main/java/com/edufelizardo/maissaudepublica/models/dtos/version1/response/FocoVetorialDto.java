package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.EspecieVetor;
import com.edufelizardo.maissaudepublica.models.enuns.FaseFoco;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoLaboratorioFoco;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoFoco;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDeposito;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Um foco do vetor (ADR-0162), com o imóvel e o local para o mapa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FocoVetorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID vistoriaId;
    private UUID imovelId;
    private String endereco;
    private String microareaCodigo;
    private String areaNome;
    private Double latitude;
    private Double longitude;
    private LocalDate data;
    private TipoDeposito tipoDeposito;
    private FaseFoco fase;
    private EspecieVetor especieSuspeita;
    private boolean amostraColetada;
    private ResultadoLaboratorioFoco resultadoLaboratorio;
    private Instant resultadoEm;
    private SituacaoFoco situacao;
    private Instant encerradoEm;
    private String observacao;
    /** Conta nos índices (não é negativo no laboratório). */
    private boolean contaNosIndices;
}
