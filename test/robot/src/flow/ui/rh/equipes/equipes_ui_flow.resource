*** Settings ***
Library     String
Resource    ../../../../pom/ui/rh/equipes/equipes_ui_pom.resource
Resource    ../../../equipes/equipe/equipe_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Equipes, unidades e lotações de apoio são criadas pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Equipes - Detalhe Deve Ser
    [Documentation]    Confere o nome no detalhe.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${titulo}=    UI Equipes - Ler Titulo Do Detalhe
    Should Be Equal    ${titulo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipes - Selecionar Equipe No Cartao
    [Documentation]    Clica no cartão da equipe e vê o detalhe com as microáreas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    UI Equipes - Abrir Na Unidade    ${unidadeId}
    UI Equipes - Clicar No Cartao    ${nome}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipes - Detalhe Deve Ser    ${nome}
    Esperar Texto Aparecer    M-14
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipes - Cadastrar Equipe
    [Documentation]    Cadastra uma eSF pela gaveta e vê o aviso e o detalhe.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    UI SF ${sufixo}
    UI Equipes - Abrir
    Clicar Botao    Nova equipe
    Escolher Opcao Da Gaveta    unidadeId    ${unidadeId}
    Escolher Opcao Da Gaveta    tipo    ESF
    Preencher Campo Da Gaveta    nome    ${nome}
    Preencher Campo Da Gaveta    microareas    21, 22
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Equipe cadastrada
    Wait Until Keyword Succeeds    10s    500ms    UI Equipes - Detalhe Deve Ser    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipes - Gaveta Recusa Dados Invalidos
    [Documentation]    Envia a gaveta sem nome e com INE curto e vê as mensagens.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Equipes - Abrir
    Clicar Botao    Nova equipe
    Preencher Campo Da Gaveta    ine    123
    Enviar Gaveta
    ${erroNome}=    Ler Erro Do Campo Da Gaveta    nome
    Should Contain    ${erroNome}    nome da equipe
    ${erroIne}=    Ler Erro Do Campo Da Gaveta    ine
    Should Contain    ${erroIne}    10 dígitos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipes - Adicionar Membro
    [Documentation]    Inclui um profissional lotado pela gaveta e confere pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    UI Equipes - Abrir Equipe    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipes - Detalhe Deve Ser    ${nome}
    Clicar Botao    Adicionar membro
    Escolher Opcao Da Gaveta    profissionalMatricula    ${matricula}
    Escolher Opcao Da Gaveta    funcao    MEDICO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Membro incluído
    ${equipe}=    GET Equipe By Id    ${uuid}
    Length Should Be    ${equipe.json()}[membros]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipes - Registrar Saida
    [Documentation]    Registra a saída de um membro pela gaveta e confere pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    funcao=ACS    microarea=14
    ${incluido}=    POST Membro Equipe    ${uuid}    ${payload}
    Should Be Equal As Integers    ${incluido.status_code}    200
    UI Equipes - Abrir Equipe    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipes - Detalhe Deve Ser    ${nome}
    UI Equipes - Clicar Saida Do Primeiro Membro
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoSaida
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoSaida    Transferência de unidade
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Saída registrada
    ${equipe}=    GET Equipe By Id    ${uuid}
    Length Should Be    ${equipe.json()}[membros]    0
    Should Be Equal    ${equipe.json()}[antigos][0][motivoSaida]    Transferência de unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipes - Atribuir Equipe Vindo De Profissionais
    [Documentation]    Abre pela ação "Atribuir equipe" e inclui o profissional na equipe da unidade.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    UI Equipes - Abrir Inclusao De Profissional    ${matricula}    ${unidadeId}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Incluir em equipe
    Escolher Opcao Da Gaveta    equipeId    ${uuid}
    Escolher Opcao Da Gaveta    funcao    ENFERMEIRO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Membro incluído
    ${equipe}=    GET Equipe By Id    ${uuid}
    Should Be Equal    ${equipe.json()}[membros][0][matricula]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
