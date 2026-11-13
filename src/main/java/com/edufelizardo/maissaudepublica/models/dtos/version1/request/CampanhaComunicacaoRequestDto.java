package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.PublicoAlvoCampanha;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Campanha de comunicação (ADR-0174). Sem território, vale para a rede. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CampanhaComunicacaoRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 150, message = "O nome vai até 150 caracteres.")
    private String nome;
    @NotBlank(message = "Informe o objetivo.")
    @Size(max = 500, message = "O objetivo vai até 500 caracteres.")
    private String objetivo;
    @Size(max = 2000, message = "A descrição vai até 2000 caracteres.")
    private String descricao;
    @NotNull(message = "Informe o início.")
    private LocalDate inicio;
    @NotNull(message = "Informe o fim.")
    private LocalDate fim;
    @NotNull(message = "Informe o público-alvo.")
    private PublicoAlvoCampanha publicoAlvo;
    @Size(max = 60, message = "A faixa etária vai até 60 caracteres.")
    private String faixaEtaria;
    private UUID unidadeId;
    private List<UUID> territorioIds;
    @Size(max = 10, message = "Até dez temas.")
    private List<@NotBlank @Size(max = 100) String> temas;
    private List<UUID> canalIds;
    private List<UUID> conteudoGrupoIds;
    private UUID programaId;
    private UUID campanhaOperacionalId;
    /** C6 (ADR-0196): as metas, opcionais. */
    @Positive(message = "A meta de pessoas é maior que zero.")
    private Integer metaPessoas;
    @Positive(message = "A meta de ações é maior que zero.")
    private Integer metaAcoes;
}
