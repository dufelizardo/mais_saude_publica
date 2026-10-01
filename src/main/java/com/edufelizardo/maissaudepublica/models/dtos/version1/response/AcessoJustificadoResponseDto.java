package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AcessoJustificado;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoAcessoJustificado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** O acesso justificado registrado (ADR-0076). Não devolve o texto: quem escreveu já o tem. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AcessoJustificadoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID pacienteUuid;
    private MotivoAcessoJustificado motivo;
    private Instant concedidoEm;
    private Instant expiraEm;

    public static AcessoJustificadoResponseDto fromAcessoJustificado(AcessoJustificado a) {
        return new AcessoJustificadoResponseDto(a.getUuid(), a.getPaciente().getUuid(), a.getMotivo(),
                a.getConcedidoEm(), a.getExpiraEm());
    }
}
