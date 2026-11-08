*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../estoque/estoque_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Estoque e almoxarifado (ADR-0177).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Estoque - Material, Entrada E Saida
    [Documentation]    Cadastra o material pela tela (o código é obrigatório), registra a entrada com lote e a saída.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${nome}=    Set Variable    UI Seringa ${sufixo}
    Ir Para Rota Do Frontend    /administrativo/estoque
    Wait For Elements State    css=h1 >> text=Estoque e almoxarifado    visible
    Wait For Elements State    css=select[aria-label="Unidade"] option[value="${unidade}"]    attached
    Select Options By    css=select[aria-label="Unidade"]    value    ${unidade}
    Click    id=tab-catalogo
    Clicar Botao    Novo material
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    codigoMaterial
    Should Contain    ${erro}    código
    Preencher Campo Da Gaveta    codigoMaterial    UI-${sufixo}
    Preencher Campo Da Gaveta    nomeMaterial    ${nome}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Material cadastrado
    Click    id=tab-saldo
    Clicar Botao    Nova entrada
    Wait For Elements State    css=#f-materialEntrada option >> text=${nome} · UI-${sufixo}    attached
    Select Options By    id=f-materialEntrada    label    ${nome} · UI-${sufixo}
    Preencher Campo Da Gaveta    quantidadeEntrada    20
    Preencher Campo Da Gaveta    loteEntrada    LT${sufixo}
    ${validade}=    Get Current Date    increment=300 days    result_format=%Y-%m-%d
    Fill Text    id=f-validadeEntrada    ${validade}
    Preencher Campo Da Gaveta    documentoEntrada    NF-${sufixo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Entrada registrada
    Wait For Elements State    css=ul[aria-label="Materiais em estoque"] >> text=${nome}    visible
    Click    css=button[aria-label="Saída de ${nome}"]
    Preencher Campo Da Gaveta    quantidadeSaida    30
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    quantidadeSaida
    Should Contain    ${erro}    20
    Preencher Campo Da Gaveta    quantidadeSaida    5
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label="Lotes de ${nome}"] >> text=15    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
