package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.EstrategiaVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.LocalAplicacaoVacina;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemRegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaVacina;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Registro (ou retificação) de vacinação (ADR-0147). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RegistroVacinacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private UUID pacienteId;
    @NotNull
    private UUID imunobiologicoId;
    @NotNull
    private DoseVacinal dose;
    /** A unidade que registra. */
    @NotNull
    private UUID unidadeId;
    @NotNull
    private OrigemRegistroVacinacao origem;
    /** Padrão: agora, na aplicada; obrigatória na transcrita. */
    private LocalDateTime aplicadaEm;
    private String profissionalMatricula;
    @Size(max = 40)
    private String lote;
    @Size(max = 100)
    private String fabricante;
    private ViaVacina via;
    private LocalAplicacaoVacina localAplicacao;
    private EstrategiaVacinacao estrategia;
    private UUID campanhaId;
    private Boolean gestante;
    @Size(max = 500)
    private String justificativa;
    private UUID atendimentoId;
    @Size(max = 200)
    private String localOrigem;
    @Size(max = 500)
    private String observacao;
    @Size(max = 500)
    private String motivoRetificacao;
}
