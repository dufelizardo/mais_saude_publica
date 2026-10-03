*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/equipamentos/equipamentos_ui_pom.resource
Resource    ../../../equipamentos/rede_unidades/rede_unidades_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    As unidades de apoio são criadas pela API (rotas por id); a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Equipamentos - Selecionar Unidade Na Lista
    [Documentation]    Busca uma unidade, clica nela e vê o painel com o nome e o CNES.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cnes}=    Generate Random String    7    [NUMBERS]
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id    ${cnes}
    UI Equipamentos - Abrir
    UI Equipamentos - Buscar    ${nome}
    UI Equipamentos - Selecionar Na Lista    ${nome}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    ${formatado}=    Evaluate    f"{$cnes[:1]} {$cnes[1:4]} {$cnes[4:]}"
    Esperar Texto Aparecer    ${formatado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Painel Deve Ser
    [Documentation]    Confere o nome no painel.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${titulo}=    UI Equipamentos - Ler Titulo Do Painel
    Should Be Equal    ${titulo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Cadastrar Unidade
    [Documentation]    Cadastra uma unidade pela gaveta e vê o aviso e o painel.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    UI UBS ${sufixo}
    UI Equipamentos - Abrir
    Clicar Botao    Nova unidade
    Preencher Campo Da Gaveta    nome    ${nome}
    Escolher Opcao Da Gaveta    tipo    UBS
    Preencher Campo Da Gaveta    logradouro    Rua das Acácias
    Preencher Campo Da Gaveta    numeroLogradouro    488
    Preencher Campo Da Gaveta    bairro    Vila Esperança
    Preencher Campo Da Gaveta    cep    04501-200
    Preencher Campo Da Gaveta    cidade    São Paulo
    Preencher Campo Da Gaveta    estado    SP
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Unidade cadastrada
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Gaveta Recusa Dados Invalidos
    [Documentation]    Envia a gaveta sem nome e com CNES curto e vê as mensagens.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Equipamentos - Abrir
    Clicar Botao    Nova unidade
    Preencher Campo Da Gaveta    cnes    123
    Enviar Gaveta
    ${erroNome}=    Ler Erro Do Campo Da Gaveta    nome
    Should Contain    ${erroNome}    nome da unidade
    ${erroCnes}=    Ler Erro Do Campo Da Gaveta    cnes
    Should Contain    ${erroCnes}    7 dígitos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Definir Horario
    [Documentation]    Abre a unidade, edita o horário de segunda e vê o turno salvo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Aba    Operacional
    Clicar Botao    Editar horário
    UI Equipamentos - Marcar Dia Aberto    Segunda
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Horário salvo
    ${ficha}=    GET Ficha Unidade    ${uuid}
    Length Should Be    ${ficha.json()}[turnos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Mudar Situacao
    [Documentation]    Põe a unidade em obra pela gaveta e vê o aviso no painel.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Aba    Operacional
    Clicar Botao    Mudar situação
    UI Equipamentos - Escolher Situacao    Em obra
    Preencher Campo Da Gaveta    motivoSituacao    Reforma do telhado
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Situação atualizada
    Esperar Texto Aparecer    Reforma do telhado
    ${ficha}=    GET Ficha Unidade    ${uuid}
    Should Be Equal    ${ficha.json()}[resumo][situacaoOperacional]    EM_OBRA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Editar Unidade
    [Documentation]    Edita o nome pela gaveta e vê o painel atualizado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Botao    Editar
    Preencher Campo Da Gaveta    nome    ${nome} Centro
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Unidade atualizada
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome} Centro
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
