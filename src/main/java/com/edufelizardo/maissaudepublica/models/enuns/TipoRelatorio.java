package com.edufelizardo.maissaudepublica.models.enuns;

import lombok.Getter;

/** O catálogo de relatórios (ADR-0167): fixo no código; cada um lê os números do domínio que responde por eles. */
@Getter
public enum TipoRelatorio {
    PRODUCAO("Produção assistencial", "Assistencial", "Atendimentos, agenda, faltas e encaixes do período, com as distribuições."),
    SITUACAO("Situação operacional", "Operacional", "Tempo de espera do período, ocupação das agendas e ocorrências da escala nos próximos dias."),
    PROGRAMAS("Indicadores dos programas", "Programas", "Inscritos, novos, saídas, faltosos, acompanhados em dia e metas de cada programa."),
    VACINACAO("Cobertura vacinal", "Epidemiológico", "Doses aplicadas, cobertura das vacinas de referência e campanhas."),
    VIGILANCIA("Vigilância em saúde", "Epidemiológico", "Notificações por agravo e por semana, oportunidade de encerramento e regularidade.");

    private final String nome;
    private final String categoria;
    private final String descricao;

    TipoRelatorio(String nome, String categoria, String descricao) {
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
    }
}
