package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaControle;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaControle;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Uma medida de controle (ADR-0131). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MedidaControleDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID surtoId;
    private UUID notificacaoId;
    private TipoMedidaControle tipo;
    private String descricao;
    private String responsavelNome;
    private LocalDate prazo;
    /** Aberta (planejada ou em andamento) com o prazo vencido. */
    private boolean vencida;
    private SituacaoMedidaControle situacao;
    private String resultado;
    private Instant criadaEm;
    private Instant concluidaEm;
}
