package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContato;
import com.edufelizardo.maissaudepublica.models.enuns.TipoExposicaoContato;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** Contato de um caso (ADR-0130): o paciente, ou nome e contato de quem ainda não tem cadastro. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ContatoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** Com cadastro; sem ele, o nome é obrigatório. */
    private UUID pacienteId;

    @Size(max = 200, message = "O nome vai até 200 caracteres.")
    private String nome;

    @Size(max = 40, message = "O telefone vai até 40 caracteres.")
    private String telefone;

    @Size(max = 500, message = "O endereço vai até 500 caracteres.")
    private String endereco;

    @NotNull(message = "Informe o tipo de exposição.")
    private TipoExposicaoContato tipoExposicao;

    private LocalDate dataExposicao;

    /** Na edição; no cadastro, em acompanhamento. */
    private SituacaoContato situacao;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;
}
