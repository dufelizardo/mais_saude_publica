*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../estoque/estoque_flow.resource
Resource    ../../../estoque/central_estoque_flow.resource
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

UI Estoque - Abrir Na Unidade
    [Documentation]    Abre a tela Estoque na unidade pedida.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    [Arguments]    ${unidade}
    Ir Para Rota Do Frontend    /administrativo/estoque
    Wait For Elements State    css=h1 >> text=Estoque e almoxarifado    visible
    Wait For Elements State    css=select[aria-label="Unidade"] option[value="${unidade}"]    attached
    Select Options By    css=select[aria-label="Unidade"]    value    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Estoque - Atender Requisicao Pela Central
    [Documentation]    A central atende a requisição enviada pela aba Requisições (ADR-0189).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}    ${unidade}    ${material}    ${matricula}=    Seed Central De Abastecimento
    ${id}=    Requisicao Enviada    ${central}    ${unidade}    ${material}    ${matricula}    ${10}
    ${u}=    GET Estoque Central    requisicao/${id}
    UI Estoque - Abrir Na Unidade    ${central}
    Click    id=tab-requisicoes
    Clicar Botao    Atender a requisição de ${u.json()}[unidadeNome]
    Fill Text    id=f-matriculaCentral    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Requisição atendida
    ${d}=    GET Estoque Central    requisicao/${id}
    Should Be Equal    ${d.json()}[situacao]    ATENDIDA
    Should Be Equal As Integers    ${d.json()}[itens][0][quantidadeAtendida]    10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Estoque - Abrir E Fechar Inventario
    [Documentation]    Abre o inventário, conta os lotes e fecha pela aba Inventário (ADR-0189).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}    ${unidade}    ${material}    ${matricula}=    Seed Central De Abastecimento
    UI Estoque - Abrir Na Unidade    ${central}
    Click    id=tab-inventario
    Clicar Botao    Abrir inventário
    Fill Text    id=f-matriculaCentral    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Inventário aberto
    Click    css=button[aria-label^="Abrir o inventário de"]
    ${campos}=    Get Elements    css=app-drawer input[aria-label^="Contagem de"]
    FOR    ${c}    IN    @{campos}
        Fill Text    ${c}    7
    END
    Fill Text    id=f-matriculaCentral    ${matricula}
    Clicar Botao    Salvar contagem
    Wait For Elements State    css=app-drawer >> text=2 de 2 contado(s)    visible
    Clicar Botao    Fechar inventário
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Inventário fechado
    ${lista}=    GET Estoque Central    inventario?unidadeId=${central}
    Should Be Equal    ${lista.json()}[0][situacao]    FECHADO
    Should Be Equal As Integers    ${lista.json()}[0][comDiferenca]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
