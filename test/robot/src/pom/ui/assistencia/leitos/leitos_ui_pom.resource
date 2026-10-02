*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Leitos (ADR-0099): mapa por setor, internações, cadastro de leitos e as gavetas de internação, detalhe, troca,
    alta, bloqueio e leito.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Leitos - Abrir Aba
    [Documentation]    Abre a tela Leitos na aba pedida (mapa, internacoes ou leitos).
    [Arguments]    ${aba}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/leitos?aba=${aba}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Abrir Internar Pelo Link
    [Documentation]    Abre a internação como o botão "Internar" do atendimento abre.
    [Arguments]    ${pacienteId}    ${unidadeId}    ${atendimentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/leitos?acao=internar&pacienteId=${pacienteId}&unidadeId=${unidadeId}&atendimentoId=${atendimentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Ler Valor Do Campo
    [Documentation]    Devolve o valor atual de um campo da gaveta (id f-<campo>).
    [Arguments]    ${campo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    Get Property    id=f-${campo}    value
    RETURN    ${valor}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
