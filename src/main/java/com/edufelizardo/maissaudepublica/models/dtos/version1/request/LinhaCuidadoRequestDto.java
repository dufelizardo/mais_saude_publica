package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.edufelizardo.maissaudepublica.models.enuns.ReferenciaPrazoEtapa;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEtapaLinhaCuidado;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Cadastro ou ajuste da linha de cuidado com as etapas (ADR-0152): a etapa com uuid é ajustada; a nova é criada; a que falta é desativada. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LinhaCuidadoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o código da linha.")
    @Size(max = 60)
    private String codigo;

    @NotBlank(message = "Informe o nome da linha.")
    @Size(max = 200)
    private String nome;

    @Size(max = 1000)
    private String descricao;

    private UUID programaId;

    private Boolean ativa;

    @Valid
    @NotEmpty(message = "A linha precisa de pelo menos uma etapa.")
    private List<Etapa> etapas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Etapa implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private UUID uuid;

        @NotBlank(message = "Informe o nome da etapa.")
        @Size(max = 200)
        private String nome;

        @NotNull(message = "Informe o tipo da etapa.")
        private TipoEtapaLinhaCuidado tipo;

        private UUID especialidadeId;
        private UUID exameId;
        private UUID procedimentoId;
        private UUID imunobiologicoId;

        @Size(max = 200)
        private String cids;

        @Min(value = 1, message = "A quantidade é de pelo menos 1.")
        private Integer quantidade;

        @PositiveOrZero(message = "O prazo não pode ser negativo.")
        private Integer prazoDias;

        private ReferenciaPrazoEtapa prazoDesde;

        private Boolean obrigatoria;
    }
}
