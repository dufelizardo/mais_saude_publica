package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.EstrategiaVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.LocalAplicacaoVacina;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemRegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaVacina;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/** Uma dose registrada (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroVacinacaoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteId;
    private String pacienteNome;
    private UUID imunobiologicoId;
    private String imunobiologicoNome;
    private String sigla;
    private DoseVacinal dose;
    private String doseRotulo;
    private LocalDateTime aplicadaEm;
    private UUID unidadeId;
    private String unidadeNome;
    private String profissionalMatricula;
    private String profissionalNome;
    private OrigemRegistroVacinacao origem;
    private String localOrigem;
    private String lote;
    private String fabricante;
    private ViaVacina via;
    private LocalAplicacaoVacina localAplicacao;
    private EstrategiaVacinacao estrategia;
    private UUID campanhaId;
    private String campanhaNome;
    private boolean gestante;
    private String justificativa;
    private String observacao;
    private UUID retificacaoDeId;
    private String motivoRetificacao;
    private Instant registradoEm;
    /** O lote do estoque e o frasco aberto de onde a dose saiu (ADR-0148). */
    private UUID loteEstoqueId;
    private UUID frascoId;
}
