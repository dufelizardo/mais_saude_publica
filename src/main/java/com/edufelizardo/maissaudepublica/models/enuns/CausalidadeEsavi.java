package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * A causalidade do ESAVI pela classificação da OMS (ADR-0150), no encerramento. Ela dá a classificação final da
 * notificação: as associações consistentes (A) confirmam, a coincidência (C) descarta, e o indeterminado (B) e o
 * inclassificável (D) ficam inconclusivos.
 */
public enum CausalidadeEsavi {
    A1_REACAO_PRODUTO(ClassificacaoCasoEpidemiologico.CONFIRMADO),
    A2_DESVIO_QUALIDADE(ClassificacaoCasoEpidemiologico.CONFIRMADO),
    A3_ERRO_IMUNIZACAO(ClassificacaoCasoEpidemiologico.CONFIRMADO),
    A4_ANSIEDADE(ClassificacaoCasoEpidemiologico.CONFIRMADO),
    B_INDETERMINADO(ClassificacaoCasoEpidemiologico.INCONCLUSIVO),
    C_COINCIDENTE(ClassificacaoCasoEpidemiologico.DESCARTADO),
    D_INCLASSIFICAVEL(ClassificacaoCasoEpidemiologico.INCONCLUSIVO);

    private final ClassificacaoCasoEpidemiologico classificacao;

    CausalidadeEsavi(ClassificacaoCasoEpidemiologico classificacao) {
        this.classificacao = classificacao;
    }

    public ClassificacaoCasoEpidemiologico classificacao() {
        return classificacao;
    }
}
