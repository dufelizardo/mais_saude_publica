*** Settings ***
Library    String
Resource    ../../../../pom/ui/administrativo/modelo_administrativo/modelo_administrativo_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas e asserções da tela Modelo administrativo.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Modelo - Tela Abre Na Aba Capacidades
    [Documentation]    Título e aba Capacidades selecionada.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Modelo - Abrir Tela
    ${titulo}=    Ler Titulo Da Pagina
    Should Be Equal    ${titulo}    Modelo administrativo
    ${aba}=    Ler Aba Selecionada
    Should Start With    ${aba}    Capacidades
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo - Rota Antiga Abre A Aba
    [Documentation]    Uma rota antiga redireciona para o Modelo na aba certa.
    [Arguments]    ${rota}    ${aba_esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Modelo - Abrir Rota    ${rota}
    ${url}=    Ler Url Atual
    Should Contain    ${url}    /administrativo/modelo
    ${aba}=    Ler Aba Selecionada
    Should Start With    ${aba}    ${aba_esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo - Validacao Da Gaveta De Capacidade
    [Documentation]    Enviar vazio mostra os erros de código e nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Modelo - Abrir Tela
    UI Modelo - Abrir Gaveta De Nova Capacidade
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    codigo
    Should Be Equal    ${erro}    Informe o código.
    ${erro}=    Ler Erro Do Campo Da Gaveta    nome
    Should Be Equal    ${erro}    Informe o nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo - Cadastrar Capacidade Pela Gaveta
    [Documentation]    Cadastra uma capacidade e vê o aviso de sucesso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    UI Modelo - Abrir Tela
    UI Modelo - Abrir Gaveta De Nova Capacidade
    Preencher Campo Da Gaveta    codigo    UI_CAP_${sufixo}
    Preencher Campo Da Gaveta    nome    Robot UI Capacidade ${sufixo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Capacidade cadastrada
    Esperar Texto Aparecer    Robot UI Capacidade ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
