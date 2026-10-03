package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Uma unidade na rede (ADR-0101): o cartão da tela Equipamentos de Saúde. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RedeUnidadeResumoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private TipoUnidadeDeSaude tipo;
    private String cnes;
    private SituacaoOperacional situacaoOperacional;
    private String motivoSituacao;
    private LocalDate previsaoRetorno;
    private boolean ativo;
    private boolean funciona24h;
    private String municipio;
    private String estado;
    private UUID unidadeSuperiorId;
    private String unidadeSuperiorNome;
    private UUID supervisaoRegionalId;
    private String supervisaoRegionalNome;
    private String endereco;
    private String telefone;
    private String email;
    private long profissionaisLotados;
    private long setores;
    private long leitos;
    private long leitosOcupados;
    /** Tem horário estruturado (pelo menos um turno). */
    private boolean comHorario;
}
