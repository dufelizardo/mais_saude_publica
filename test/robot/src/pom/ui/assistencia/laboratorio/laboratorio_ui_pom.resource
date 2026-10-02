*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Laboratório (ADR-0094): abas Para coletar, Em análise, Para liberar, Pedidos e Catálogo de exames, gavetas
    de pedido, coleta, resultado, liberação, rejeição de amostra, detalhe, retificação, cancelamento e exame.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Laboratorio - Abrir Aba
    [Documentation]    Abre a tela Laboratório já na aba pedida (coletar, analise, liberar, pedidos ou catalogo).
    [Arguments]    ${aba}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/laboratorio?aba=${aba}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Abrir Pedido Pelo Link
    [Documentation]    Abre o pedido de exames como o botão "Pedir exames" do atendimento abre.
    [Arguments]    ${pacienteId}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/laboratorio?acao=pedido&pacienteId=${pacienteId}&unidadeId=${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Marcar Na Gaveta
    [Documentation]    Marca uma caixa da gaveta pelo texto do rótulo (exame do pedido ou da coleta).
    [Arguments]    ${rotulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Click    css=app-drawer label >> text=${rotulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Ler Valor Do Campo
    [Documentation]    Devolve o valor atual de um campo da gaveta (id f-<campo>).
    [Arguments]    ${campo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    Get Property    id=f-${campo}    value
    RETURN    ${valor}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
