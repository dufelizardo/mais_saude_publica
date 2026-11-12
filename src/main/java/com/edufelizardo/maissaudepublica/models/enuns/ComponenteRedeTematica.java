package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Os componentes de cada rede temática que são pontos de atendimento (GR2, ADR-0197). Ficam fora a Força Nacional, a governança e a
 * regulação como função, que não são unidades.
 */
public enum ComponenteRedeTematica {
    RUE_ATENCAO_BASICA(RedeTematica.URGENCIA_EMERGENCIA, "Atenção básica"),
    RUE_SAMU(RedeTematica.URGENCIA_EMERGENCIA, "SAMU 192 e central de regulação"),
    RUE_SALA_ESTABILIZACAO(RedeTematica.URGENCIA_EMERGENCIA, "Sala de estabilização"),
    RUE_UPA_24H(RedeTematica.URGENCIA_EMERGENCIA, "UPA 24h e serviços de urgência 24 horas"),
    RUE_HOSPITALAR(RedeTematica.URGENCIA_EMERGENCIA, "Atenção hospitalar"),
    RUE_ATENCAO_DOMICILIAR(RedeTematica.URGENCIA_EMERGENCIA, "Atenção domiciliar"),
    RC_PRE_NATAL(RedeTematica.CEGONHA_ALYNE, "Pré-natal"),
    RC_PARTO_NASCIMENTO(RedeTematica.CEGONHA_ALYNE, "Parto e nascimento"),
    RC_PUERPERIO_CRIANCA(RedeTematica.CEGONHA_ALYNE, "Puerpério e atenção integral à saúde da criança"),
    RC_SISTEMA_LOGISTICO(RedeTematica.CEGONHA_ALYNE, "Sistema logístico (transporte sanitário)"),
    PCD_ATENCAO_BASICA(RedeTematica.PESSOA_COM_DEFICIENCIA, "Atenção básica"),
    PCD_REABILITACAO(RedeTematica.PESSOA_COM_DEFICIENCIA, "Atenção especializada em reabilitação"),
    PCD_HOSPITALAR_URGENCIA(RedeTematica.PESSOA_COM_DEFICIENCIA, "Atenção hospitalar e de urgência"),
    DC_ATENCAO_BASICA(RedeTematica.DOENCAS_CRONICAS, "Atenção básica"),
    DC_ESPECIALIZADA(RedeTematica.DOENCAS_CRONICAS, "Atenção especializada ambulatorial e hospitalar"),
    DC_SISTEMAS_APOIO(RedeTematica.DOENCAS_CRONICAS, "Sistemas de apoio (diagnóstico e assistência farmacêutica)"),
    DC_SISTEMAS_LOGISTICOS(RedeTematica.DOENCAS_CRONICAS, "Sistemas logísticos (transporte sanitário)");

    private final RedeTematica rede;
    private final String rotulo;

    ComponenteRedeTematica(RedeTematica rede, String rotulo) {
        this.rede = rede;
        this.rotulo = rotulo;
    }

    public RedeTematica rede() {
        return rede;
    }

    public String rotulo() {
        return rotulo;
    }
}
