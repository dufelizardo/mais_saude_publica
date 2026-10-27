package com.edufelizardo.maissaudepublica.models.enuns;

import java.util.Set;

/**
 * Os indicadores programáticos calculados (ADR-0141): um conjunto fixo, com a regra no código, e não fórmulas
 * cadastráveis. Cada um diz a que programas se aplica (vazio: a todos) e se precisa do exame da meta.
 */
public enum IndicadorProgramatico {
    ACOMPANHADOS_EM_DIA("Acompanhados em dia", "Inscritos com atendimento realizado dentro da periodicidade do programa, sobre os inscritos "
            + "ativos no fim do período.", TipoValorIndicador.PERCENTUAL, Set.of(), false),
    COBERTURA("Cobertura", "Inscritos ativos sobre os elegíveis adscritos às equipes (pela idade e pelo sexo do público); apurada hoje.",
            TipoValorIndicador.PERCENTUAL, Set.of(), false),
    ACOES_REALIZADAS("Ações realizadas", "Ações programáticas realizadas sobre as previstas no período (sem as canceladas).",
            TipoValorIndicador.PERCENTUAL, Set.of(), false),
    PA_CONTROLADA("PA controlada", "Inscritos cuja última pressão arterial aferida no período ficou abaixo de 140/90 mmHg, sobre os inscritos com "
            + "pressão aferida no período.", TipoValorIndicador.PERCENTUAL, Set.of("HIPERDIA"), false),
    EXAME_NO_ALVO("Exame no alvo", "Inscritos cujo último resultado do exame da meta, no período, ficou dentro do alvo, sobre os inscritos com "
            + "o exame no período (ex.: HbA1c abaixo de 7%).", TipoValorIndicador.PERCENTUAL, Set.of(), true),
    PRE_NATAL_6_CONSULTAS("Pré-natal com 6 consultas", "Gestantes inscritas com 6 ou mais atendimentos desde a inscrição, sobre as gestantes "
            + "inscritas no fim do período.", TipoValorIndicador.PERCENTUAL, Set.of("PRE_NATAL"), false),
    TRATAMENTO_ODONTOLOGICO_CONCLUIDO("Tratamento odontológico concluído", "Planos de tratamento concluídos sobre os abertos no período.",
            TipoValorIndicador.PERCENTUAL, Set.of("SAUDE_BUCAL"), false),
    PROTESES_ENTREGUES("Próteses entregues", "Próteses dentárias entregues no período.", TipoValorIndicador.QUANTIDADE, Set.of("SAUDE_BUCAL"), false),
    ESCOVACAO_SUPERVISIONADA("Escovação supervisionada", "Pessoas atendidas em ações coletivas com escovação supervisionada no período.",
            TipoValorIndicador.QUANTIDADE, Set.of("SAUDE_BUCAL", "SAUDE_NA_ESCOLA"), false);

    private final String nome;
    private final String descricao;
    private final TipoValorIndicador tipo;
    private final Set<String> programas;
    private final boolean precisaExame;

    IndicadorProgramatico(String nome, String descricao, TipoValorIndicador tipo, Set<String> programas, boolean precisaExame) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.programas = programas;
        this.precisaExame = precisaExame;
    }

    public String nome() {
        return nome;
    }

    public String descricao() {
        return descricao;
    }

    public TipoValorIndicador tipo() {
        return tipo;
    }

    public boolean precisaExame() {
        return precisaExame;
    }

    /** Vale para o programa (pelo código; sem lista, para todos). */
    public boolean aplicaA(String codigoPrograma) {
        return programas.isEmpty() || programas.contains(codigoPrograma);
    }
}
