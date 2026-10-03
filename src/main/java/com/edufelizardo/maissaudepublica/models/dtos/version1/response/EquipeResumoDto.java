package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import java.util.List;
import java.util.UUID;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/** Uma equipe no cartão da tela (ADR-0103), com a composição calculada. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EquipeResumoDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private String nome;
    private TipoEquipe tipo;
    private String ine;
    private boolean ativa;
    private UUID unidadeId;
    private String unidadeNome;
    private String microareas;
    private String coordenadorNome;
    private int membros;
    private int agentesComunitarios;
    /** Composição mínima do tipo atendida. Tipo sem composição mínima: verdadeiro. */
    private boolean completa;
    /** As funções que faltam para a composição mínima, uma por item (ex.: "ACS", "TECNICO_ENFERMAGEM ou AUXILIAR_ENFERMAGEM"). */
    private List<String> faltando;
    /** Nomes dos membros vigentes, para os avatares. */
    private List<String> nomesMembros;
    private int equipesApoiadas;
}
