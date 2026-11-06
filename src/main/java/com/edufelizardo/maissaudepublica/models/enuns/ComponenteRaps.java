package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Os componentes da Rede de Atenção Psicossocial (Portaria de Consolidação GM/MS nº 3/2017, anexo V), que classificam
 * os pontos da rede (ADR-0153). Vêm dos perfis e das capacidades da unidade.
 */
public enum ComponenteRaps {
    ATENCAO_BASICA("Atenção básica"),
    ATENCAO_PSICOSSOCIAL_ESPECIALIZADA("Atenção psicossocial especializada"),
    URGENCIA_EMERGENCIA("Urgência e emergência"),
    RESIDENCIAL_TRANSITORIO("Atenção residencial de caráter transitório"),
    HOSPITALAR("Atenção hospitalar"),
    DESINSTITUCIONALIZACAO("Estratégias de desinstitucionalização"),
    REABILITACAO_PSICOSSOCIAL("Reabilitação psicossocial");

    private final String rotulo;

    ComponenteRaps(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
