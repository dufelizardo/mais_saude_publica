package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.GrauRiscoSanitario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Cadastro do estabelecimento regulado (ADR-0199): CPF ou CNPJ com o dígito verificador (400), ao menos uma atividade com o grau de risco. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EstabelecimentoReguladoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o CPF ou o CNPJ.")
    @Size(max = 20, message = "O CPF ou o CNPJ vai até 20 caracteres.")
    private String documento;
    @NotBlank(message = "Informe a razão social ou o nome.")
    @Size(max = 200, message = "A razão social vai até 200 caracteres.")
    private String razaoSocial;
    @Size(max = 200, message = "O nome fantasia vai até 200 caracteres.")
    private String nomeFantasia;
    @NotBlank(message = "Informe o endereço.")
    @Size(max = 300, message = "O endereço vai até 300 caracteres.")
    private String endereco;
    @DecimalMin(value = "-90", message = "A latitude vai de -90 a 90.")
    @DecimalMax(value = "90", message = "A latitude vai de -90 a 90.")
    private Double latitude;
    @DecimalMin(value = "-180", message = "A longitude vai de -180 a 180.")
    @DecimalMax(value = "180", message = "A longitude vai de -180 a 180.")
    private Double longitude;
    private UUID microareaId;
    /** A unidade pública inspecionada, quando o estabelecimento é da rede. */
    private UUID unidadeId;
    @Size(max = 30, message = "O telefone vai até 30 caracteres.")
    private String telefone;
    @Size(max = 200, message = "O responsável legal vai até 200 caracteres.")
    private String responsavelLegal;
    @Size(max = 200, message = "O responsável técnico vai até 200 caracteres.")
    private String responsavelTecnico;
    @Size(max = 50, message = "O registro no conselho vai até 50 caracteres.")
    private String registroConselhoRt;
    /** Só na alteração; o cadastro nasce ativo. */
    private Boolean ativo;
    @NotEmpty(message = "Informe ao menos uma atividade.")
    @Valid
    private List<Atividade> atividades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Atividade implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotBlank(message = "Informe o CNAE da atividade.")
        @Pattern(regexp = "\\d{4}-?\\d/?\\d{2}|\\d{7}", message = "O CNAE tem sete dígitos (0000-0/00).")
        private String cnae;
        @NotBlank(message = "Descreva a atividade.")
        @Size(max = 200, message = "A descrição da atividade vai até 200 caracteres.")
        private String descricao;
        @NotNull(message = "Informe o grau de risco da atividade.")
        private GrauRiscoSanitario risco;
    }
}
