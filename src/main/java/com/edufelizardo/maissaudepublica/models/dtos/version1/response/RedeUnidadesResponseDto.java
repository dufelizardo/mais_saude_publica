package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import java.util.List;
import java.util.Map;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** A rede de unidades no escopo, com os indicadores do topo da tela (ADR-0101). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RedeUnidadesResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Unidades de atendimento (sem os níveis de gestão federal, estadual, municipal e regional). */
    private long unidades;
    private long ativas;
    private long emOperacao;
    /** Em manutenção, em obra ou inoperantes. */
    private long foraDeOperacao;
    private Map<TipoUnidadeDeSaude, Long> porTipo;
    private List<RedeUnidadeResumoDto> rede;
}
