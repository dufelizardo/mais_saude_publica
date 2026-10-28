package com.edufelizardo.maissaudepublica.models.enuns;

/** O tipo da instituição externa (ADR-0154): o tipo diz o setor, menos OUTRO, que aceita qualquer setor. */
public enum TipoInstituicaoExterna {
    CRAS(SetorIntersetorial.ASSISTENCIA_SOCIAL),
    CREAS(SetorIntersetorial.ASSISTENCIA_SOCIAL),
    CENTRO_POP(SetorIntersetorial.ASSISTENCIA_SOCIAL),
    ACOLHIMENTO_INSTITUCIONAL(SetorIntersetorial.ASSISTENCIA_SOCIAL),
    ESCOLA(SetorIntersetorial.EDUCACAO),
    CONSELHO_TUTELAR(SetorIntersetorial.GARANTIA_DE_DIREITOS),
    DEFENSORIA_PUBLICA(SetorIntersetorial.GARANTIA_DE_DIREITOS),
    MINISTERIO_PUBLICO(SetorIntersetorial.GARANTIA_DE_DIREITOS),
    JUDICIARIO(SetorIntersetorial.GARANTIA_DE_DIREITOS),
    SERVICO_DE_EMPREGO(SetorIntersetorial.TRABALHO_E_RENDA),
    HABITACAO(SetorIntersetorial.HABITACAO),
    SEGURANCA_PUBLICA(SetorIntersetorial.SEGURANCA_PUBLICA),
    OUTRO(null);

    private final SetorIntersetorial setor;

    TipoInstituicaoExterna(SetorIntersetorial setor) {
        this.setor = setor;
    }

    /** O setor do tipo; nulo em OUTRO. */
    public SetorIntersetorial setor() {
        return setor;
    }
}
