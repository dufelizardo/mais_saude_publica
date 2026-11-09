*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../gestao/gestao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Coordenações e gerência (ADR-0175).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Coordenacoes - Abrir
    [Documentation]    Abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/coordenacoes
    Wait For Elements State    css=h1 >> text=Coordenações e gerência    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Coordenacoes - Criar Gerencia E Designar Titular
    [Documentation]    Sem nome, a mensagem; cria a gerência, abre no organograma e designa o titular.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    UI Gerência ${sufixo}
    ${matricula}=    Seed A Profissional Matricula
    UI Coordenacoes - Abrir
    Clicar Botao    Nova área
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    nomeArea
    Should Contain    ${erro}    nome
    Preencher Campo Da Gaveta    nomeArea    ${nome}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Área criada
    Wait For Elements State    css=#h-area >> text=${nome}    visible
    Clicar Botao    Designar titular
    Wait For Elements State    css=#f-profissionalArea option[value="${matricula}"]    attached
    Select Options By    id=f-profissionalArea    value    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Titular designado
    Wait For Elements State    css=ul[aria-label="Histórico de quem respondeu"] >> text=Titular    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

