*** Settings ***
Library    String
Resource    ../../../../pom/ui/administrativo/setores/setores_ui_pom.resource
Resource    ../../../setor/create_setor/create_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas e asserções da tela Setores. Os dados vêm pela API (Seed A Unidade De Saude Uuid).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Setores - Tela Abre Na Aba Setores
    [Documentation]    A tela abre com o título e a aba Setores selecionada.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Setores - Abrir Tela
    ${titulo}=    Ler Titulo Da Pagina
    Should Be Equal    ${titulo}    Setores
    ${aba}=    Ler Aba Selecionada
    Should Start With    ${aba}    Setores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Setores - Validacao Da Gaveta
    [Documentation]    Enviar a gaveta vazia mostra os erros de unidade, nome e código.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Setores - Abrir Tela
    UI Setores - Abrir Gaveta De Novo Setor
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Novo setor
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    unidadeId
    Should Be Equal    ${erro}    Escolha a unidade.
    ${erro}=    Ler Erro Do Campo Da Gaveta    nome
    Should Be Equal    ${erro}    Informe o nome do setor.
    ${erro}=    Ler Erro Do Campo Da Gaveta    codigo
    Should Be Equal    ${erro}    Informe o código.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Setores - Cadastrar Pela Gaveta
    [Documentation]    Cadastra um setor pela gaveta e o vê na lista, com o aviso de sucesso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable    Robot UI Setor ${sufixo}
    UI Setores - Abrir Tela
    UI Setores - Abrir Gaveta De Novo Setor
    Escolher Opcao Da Gaveta    unidadeId    ${unidadeId}
    Preencher Campo Da Gaveta    nome    ${nome}
    Preencher Campo Da Gaveta    codigo    UI${sufixo}
    Escolher Opcao Da Gaveta    tipo    ADMINISTRATIVO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Setor cadastrado
    ${linha}=    UI Setores - Ler Linha Do Setor    ${nome}
    Should Contain    ${linha}    UI${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Setores - Aba Responsabilidades Pela Rota Antiga
    [Documentation]    A rota antiga abre a aba Responsabilidades.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Setores - Abrir Rota Antiga De Responsabilidades
    ${url}=    Ler Url Atual
    Should Contain    ${url}    /administrativo/setores
    Should Contain    ${url}    aba=responsabilidades
    ${aba}=    Ler Aba Selecionada
    Should Start With    ${aba}    Responsabilidades
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
