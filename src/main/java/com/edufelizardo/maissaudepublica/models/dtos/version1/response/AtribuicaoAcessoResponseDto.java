package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AtribuicaoAcessoResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private UUID usuarioUuid;
    private String usuarioNome;
    private String usuarioCpf;
    private UUID papelUuid;
    private String papelCodigo;
    private String papelNome;
    /** Nulo = rede inteira. */
    private UUID unidadeUuid;
    private String unidadeNome;
    private LocalDate inicio;
    private LocalDate fim;
    private Instant concedidoEm;
    private String concedidoPorCpf;
    private Instant revogadoEm;
    private String revogadoPorCpf;
    private String motivoRevogacao;
    private boolean vigente;

    public static AtribuicaoAcessoResponseDto fromAtribuicao(AtribuicaoAcesso a) {
        return new AtribuicaoAcessoResponseDto(a.getUuid(), a.getUsuario().getUuid(), a.getUsuario().getNome(),
                a.getUsuario().getCpf(), a.getPapel().getUuid(), a.getPapel().getCodigo(), a.getPapel().getNome(),
                a.getUnidade() != null ? a.getUnidade().getUuid() : null,
                a.getUnidade() != null ? a.getUnidade().getNome() : null,
                a.getInicio(), a.getFim(), a.getConcedidoEm(), a.getConcedidoPorCpf(), a.getRevogadoEm(),
                a.getRevogadoPorCpf(), a.getMotivoRevogacao(), a.vigenteEm(LocalDate.now()));
    }
}
