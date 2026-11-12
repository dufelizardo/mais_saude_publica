package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * As redes temáticas de atenção à saúde (Portaria de Consolidação GM/MS nº 3/2017), menos a RAPS, que tem o seu painel desde a ADR-0153
 * (GR2, ADR-0197).
 */
public enum RedeTematica {
    URGENCIA_EMERGENCIA("Rede de Atenção às Urgências e Emergências"),
    CEGONHA_ALYNE("Rede Cegonha / Rede Alyne"),
    PESSOA_COM_DEFICIENCIA("Rede de Cuidados à Pessoa com Deficiência"),
    DOENCAS_CRONICAS("Rede de Atenção às Pessoas com Doenças Crônicas");

    private final String rotulo;

    RedeTematica(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
