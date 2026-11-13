package com.edufelizardo.maissaudepublica.models.enuns;

/**
 * Os indicadores de qualidade calculados dos registros da rede (ADR-0205), com o nome, a medida e o sentido: menor é melhor em todos,
 * menos na adesão aos protocolos.
 */
public enum IndicadorQualidade {
    TAXA_QUEDAS("Taxa de quedas", "por mil pacientes-dia", true),
    INCIDENCIA_LESAO_PRESSAO("Incidência de lesão por pressão", "por mil pacientes-dia", true),
    REINTERNACAO_30_DIAS("Reinternação em 30 dias", "%", true),
    EVENTOS_ADVERSOS_GRAVES("Eventos adversos graves", "por mil pacientes-dia", true),
    TEMPO_ATE_MEDICO("Tempo até o atendimento médico", "minutos (mediana)", true),
    ABSENTEISMO("Absenteísmo", "%", true),
    ADESAO_PROTOCOLO("Adesão ao protocolo", "%", false);

    private final String nome;
    private final String medida;
    private final boolean menorMelhor;

    IndicadorQualidade(String nome, String medida, boolean menorMelhor) {
        this.nome = nome;
        this.medida = medida;
        this.menorMelhor = menorMelhor;
    }

    public String nome() {
        return nome;
    }

    public String medida() {
        return medida;
    }

    public boolean menorMelhor() {
        return menorMelhor;
    }
}
