*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Regulação (ADR-0088): abas Fila, Solicitações e Procedimentos regulados, gavetas de nova solicitação,
    análise, complemento, cancelamento e catálogo.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Regulacao - Abrir Aba
    [Documentation]    Abre a tela Regulação já na aba pedida (fila, solicitacoes ou procedimentos).
    [Arguments]    ${aba}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/regulacao?aba=${aba}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Abrir Nova Solicitacao Pelo Link
    [Documentation]    Abre a nova solicitação como o botão "Encaminhar" do atendimento abre.
    [Arguments]    ${pacienteId}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/regulacao?acao=nova&pacienteId=${pacienteId}&unidadeId=${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Escolher Decisao
    [Documentation]    Marca a decisão do regulador pelo texto do rótulo.
    [Arguments]    ${rotulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Click    css=app-drawer label >> text=${rotulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Ler Valor Do Campo
    [Documentation]    Devolve o valor atual de um campo da gaveta (id f-<campo>).
    [Arguments]    ${campo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    Get Property    id=f-${campo}    value
    RETURN    ${valor}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
