package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Uma unidade que recebe urgência (perfil Urgência vigente, ADR-0176), com os leitos livres como informação para o
 * regulador e quantas ocorrências abertas já vão para ela. Sem vaga, o regulador ainda pode mandar (vaga zero).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class DestinoUrgenciaDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID unidadeId;
    private String nome;
    private TipoUnidadeDeSaude tipo;
    private long leitosLivres;
    private long leitosAtivos;
    private long aCaminho;
}
