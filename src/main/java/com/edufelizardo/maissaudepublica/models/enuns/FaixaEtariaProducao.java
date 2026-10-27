package com.edufelizardo.maissaudepublica.models.enuns;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Faixas etárias da produção assistencial (ADR-0143), as do protótipo Painel.html. A idade é a do dia do atendimento;
 * sem data de nascimento, fica sem faixa.
 */
public enum FaixaEtariaProducao {
    NEONATAL("Neonatal", "0–28 dias"),
    PRIMEIRA_INFANCIA("1ª infância", "até 5 anos"),
    CRIANCA("Crianças", "6–12 anos"),
    ADOLESCENTE("Adolescentes", "13–17 anos"),
    ADULTO("Adultos", "18–59 anos"),
    IDOSO("Idosos", "60+ anos"),
    SEM_DATA("Sem data de nascimento", "—");

    private final String rotulo;
    private final String faixa;

    FaixaEtariaProducao(String rotulo, String faixa) {
        this.rotulo = rotulo;
        this.faixa = faixa;
    }

    public String rotulo() {
        return rotulo;
    }

    public String faixa() {
        return faixa;
    }

    public static FaixaEtariaProducao de(LocalDate nascimento, LocalDate dia) {
        if (nascimento == null || nascimento.isAfter(dia)) {
            return SEM_DATA;
        }
        if (ChronoUnit.DAYS.between(nascimento, dia) <= 28) {
            return NEONATAL;
        }
        int anos = Period.between(nascimento, dia).getYears();
        if (anos <= 5) {
            return PRIMEIRA_INFANCIA;
        }
        if (anos <= 12) {
            return CRIANCA;
        }
        if (anos <= 17) {
            return ADOLESCENTE;
        }
        return anos <= 59 ? ADULTO : IDOSO;
    }
}
