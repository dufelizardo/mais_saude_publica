package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AdministracaoMedicamento;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.ViaAdministracao;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AdministracaoMedicamentoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID atendimentoUuid;
    private UUID consultaUuid;
    private UUID medicamentoUuid;
    private String medicamentoNome;
    private SituacaoAdministracao situacao;
    private UUID loteUuid;
    private String numeroLote;
    private String dose;
    private ViaAdministracao via;
    private Integer quantidade;
    private MotivoNaoAdministracao motivoNaoAdministracao;
    private String observacao;
    private LocalDateTime dataHora;
    private String profissionalMatricula;
    private String profissionalNome;

    // Retificação (ADR-0062)
    private UUID retificacaoDeUuid;
    private String motivoRetificacao;
    private Instant registradoEm;
    private String registradoPorCpf;
    private boolean retificado;
    private UUID retificadoPorUuid;

    public static AdministracaoMedicamentoResponseDto fromAdministracao(AdministracaoMedicamento a, UUID retificadoPor) {
        return new AdministracaoMedicamentoResponseDto(
                a.getUuid(),
                a.getAtendimento().getUuid(),
                a.getConsulta().getUuid(),
                a.getMedicamento().getUuid(),
                a.getMedicamento().getNome(),
                a.getSituacao(),
                a.getLote() != null ? a.getLote().getUuid() : null,
                a.getLote() != null ? a.getLote().getNumeroLote() : null,
                a.getDose(),
                a.getVia(),
                a.getQuantidade(),
                a.getMotivoNaoAdministracao(),
                a.getObservacao(),
                a.getDataHora(),
                a.getProfissional().getMatricula(),
                a.getProfissional().getNome(),
                a.getRetificacaoDe() != null ? a.getRetificacaoDe().getUuid() : null,
                a.getMotivoRetificacao(),
                a.getRegistradoEm(),
                a.getRegistradoPorCpf(),
                retificadoPor != null,
                retificadoPor
        );
    }
}
