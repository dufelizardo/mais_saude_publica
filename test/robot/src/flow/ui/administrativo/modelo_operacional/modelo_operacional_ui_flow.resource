*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/modelo_operacional/modelo_operacional_ui_pom.resource
Resource    ../equipamentos/equipamentos_ui_flow.resource
Resource    ../../../equipamentos/modelo_operacional/modelo_operacional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    As unidades e as associações de apoio são criadas pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed CAPS Com Nome
    [Documentation]    Creates a CAPS through the API and returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    UI CAPS ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    tipo=CAPS
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[resumo][uuid]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Capacidades - Incluir Perfil E Capacidade
    [Documentation]    Pela aba Capacidades, inclui Atenção psicossocial e CAPS AD num CAPS.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed CAPS Com Nome
    ${perfil}=    Id Do Perfil    ATENCAO_PSICOSSOCIAL
    ${capacidade}=    Id Da Capacidade    CAPS_AD
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Aba    Capacidades
    Clicar Botao    Adicionar perfil
    Escolher Opcao Da Gaveta    perfilId    ${perfil}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Perfil incluído
    Clicar Botao    Adicionar capacidade
    Escolher Opcao Da Gaveta    capacidadeId    ${capacidade}
    Preencher Campo Da Gaveta    observacao    Portaria de habilitação 45/2026
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Capacidade incluída
    Esperar Texto Aparecer    Portaria de habilitação 45/2026
    ${modelo}=    GET Modelo Operacional Unidade    ${uuid}
    Length Should Be    ${modelo.json()}[perfis]    1
    Should Be Equal    ${modelo.json()}[capacidades][0][codigo]    CAPS_AD
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Capacidades - Encerrar Capacidade
    [Documentation]    Encerra a capacidade pela gaveta: sem motivo, a mensagem; com motivo, vai para o histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed CAPS Com Nome
    Incluir Perfil Por Codigo    ${uuid}    ATENCAO_PSICOSSOCIAL
    Incluir Capacidade Por Codigo    ${uuid}    CAPS_II
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Aba    Capacidades
    Clicar Botao    Encerrar CAPS II
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoFim
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoFim    Habilitado como CAPS III
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Capacidade encerrada
    ${modelo}=    GET Modelo Operacional Unidade    ${uuid}
    Length Should Be    ${modelo.json()}[capacidades]    0
    Should Be Equal    ${modelo.json()}[capacidadesEncerradas][0][motivoFim]    Habilitado como CAPS III
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Capacidades - Filtrar Rede Por Perfil
    [Documentation]    O filtro de perfil da lista mostra a unidade que tem o perfil.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed CAPS Com Nome
    Incluir Perfil Por Codigo    ${uuid}    ATENCAO_PSICOSSOCIAL
    UI Equipamentos - Abrir
    UI Modelo Operacional - Filtrar Lista Da Rede Por Perfil    ATENCAO_PSICOSSOCIAL
    UI Equipamentos - Buscar    ${nome}
    Wait For Elements State    role=option[name="${nome}"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Ver Carga Inicial
    [Documentation]    A tela abre com os perfis da carga inicial e mostra as capacidades de um perfil.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Modelo Operacional - Abrir
    Esperar Texto Aparecer    Atenção psicossocial
    UI Modelo Operacional - Ver Capacidades Do Perfil    Atenção psicossocial
    Esperar Texto Aparecer    CAPS álcool e drogas (CAPS AD)
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Cadastrar Perfil
    [Documentation]    Cadastra um perfil pela gaveta e o vê na tabela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    UI Modelo Operacional - Abrir
    Clicar Botao    Novo perfil
    Preencher Campo Da Gaveta    codigo    UI_${sufixo}
    Preencher Campo Da Gaveta    nome    Hospital-dia ${sufixo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Perfil cadastrado
    UI Modelo Operacional - Buscar    ${sufixo}
    Esperar Texto Aparecer    Hospital-dia ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Gaveta Recusa Codigo Invalido
    [Documentation]    Código com espaço e nome vazio mostram as mensagens dos campos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Modelo Operacional - Abrir
    Clicar Botao    Novo perfil
    Preencher Campo Da Gaveta    codigo    COM ESPACO
    Enviar Gaveta
    ${erroCodigo}=    Ler Erro Do Campo Da Gaveta    codigo
    Should Contain    ${erroCodigo}    sublinhado
    ${erroNome}=    Ler Erro Do Campo Da Gaveta    nome
    Should Contain    ${erroNome}    nome
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
