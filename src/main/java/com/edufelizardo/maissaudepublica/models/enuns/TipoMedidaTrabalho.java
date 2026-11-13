package com.edufelizardo.maissaudepublica.models.enuns;

/** Tipo da medida recomendada pela investigação do trabalho (ADR-0208). */
public enum TipoMedidaTrabalho {
    /** Adequação do ambiente ou do processo. */
    ADEQUACAO_DO_AMBIENTE,
    /** Equipamento de proteção coletiva. */
    PROTECAO_COLETIVA,
    /** Equipamento de proteção individual. */
    PROTECAO_INDIVIDUAL,
    /** Jornada, pausas, ritmo e organização. */
    ORGANIZACAO_DO_TRABALHO,
    /** Capacitação dos trabalhadores. */
    CAPACITACAO,
    /** Afastamento do trabalhador da exposição. */
    AFASTAMENTO_DA_EXPOSICAO,
    /** Acompanhamento da saúde dos demais expostos. */
    VIGILANCIA_DOS_EXPOSTOS,
    /** Outra medida. */
    OUTRA
}
