package com.edufelizardo.maissaudepublica.models.enuns;

import java.util.List;
import java.util.Set;

/**
 * Tipo de equipe (ADR-0103), pela Política Nacional de Atenção Básica e pela Portaria GM/MS 635/2023 (eMulti, que substitui o
 * NASF-AB). Cada tipo diz a composição mínima: cada item é um conjunto de funções, e a equipe precisa de pelo menos um
 * membro de cada conjunto.
 */
public enum TipoEquipe {
    /** Equipe de Saúde da Família: médico, enfermeiro, técnico ou auxiliar de enfermagem e agente comunitário. */
    ESF(List.of(Set.of(FuncaoEquipe.MEDICO), Set.of(FuncaoEquipe.ENFERMEIRO),
            Set.of(FuncaoEquipe.TECNICO_ENFERMAGEM, FuncaoEquipe.AUXILIAR_ENFERMAGEM), Set.of(FuncaoEquipe.ACS))),
    /** Equipe de Atenção Primária (eAP): médico e enfermeiro. */
    EAB(List.of(Set.of(FuncaoEquipe.MEDICO), Set.of(FuncaoEquipe.ENFERMEIRO))),
    /** Equipe de Saúde Bucal: cirurgião-dentista e técnico ou auxiliar de saúde bucal. */
    ESB(List.of(Set.of(FuncaoEquipe.CIRURGIAO_DENTISTA), Set.of(FuncaoEquipe.TECNICO_SAUDE_BUCAL, FuncaoEquipe.AUXILIAR_SAUDE_BUCAL))),
    /** Equipe Multiprofissional (antigo NASF-AB): apoia equipes de Saúde da Família e de Atenção Primária. */
    EMULTI(List.of()),
    /** Consultório na Rua: composição varia com a modalidade. */
    ECR(List.of()),
    /** Equipe multiprofissional de CAPS. */
    CAPS_MULTI(List.of());

    private final List<Set<FuncaoEquipe>> composicaoMinima;

    TipoEquipe(List<Set<FuncaoEquipe>> composicaoMinima) {
        this.composicaoMinima = composicaoMinima;
    }

    public List<Set<FuncaoEquipe>> composicaoMinima() {
        return composicaoMinima;
    }

    /** Os profissionais da eSF e da eAP são de uma equipe só (carga horária da PNAB). */
    public boolean exclusiva() {
        return this == ESF || this == EAB;
    }
}
