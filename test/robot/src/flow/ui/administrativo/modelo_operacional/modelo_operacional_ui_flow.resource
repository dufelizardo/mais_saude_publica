*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/modelo_operacional/modelo_operacional_ui_pom.resource
Resource    ../equipamentos/equipamentos_ui_flow.resource
Resource    ../../../equipamentos/modelo_operacional/modelo_operacional_flow.resource
Resource    ../../../equipamentos/servicos_unidade/servicos_unidade_flow.resource
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

Seed Hospital Com UTI E Nome
    [Documentation]    Creates a hospital with the UTI capability and returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    UI Hospital ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    tipo=HOSPITAL
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${uuid}=    Set Variable    ${response.json()}[resumo][uuid]
    Incluir Perfil Por Codigo    ${uuid}    INTERNACAO
    Incluir Capacidade Por Codigo    ${uuid}    UTI
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Servicos - Incluir Com Setor
    [Documentation]    Pela aba Serviços, inclui a UTI Adulto ligada a um setor; sem nome, a mensagem do campo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Hospital Com UTI E Nome
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${setor}=    Set Variable    Ala ${sufixo}
    ${payload}=    Build Setor Payload    ${uuid}    ${setor}    ROBOT-${sufixo}    ASSISTENCIAL
    ${criado}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${tipo}=    Id Do Tipo De Servico    UTI
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Aba    Serviços
    Clicar Botao    Adicionar serviço
    Escolher Opcao Da Gaveta    tipoId    ${tipo}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    nomeServico
    Should Contain    ${erro}    nome
    Preencher Campo Da Gaveta    nomeServico    UTI Adulto
    UI Servicos - Marcar Setor    ${setor}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Serviço incluído
    Esperar Texto Aparecer    UTI Adulto
    ${servicos}=    GET Servicos Da Unidade    ${uuid}
    Should Be Equal    ${servicos.json()}[servicos][0][setores][0][nome]    ${setor}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Servicos - Encerrar
    [Documentation]    Encerra o serviço pela gaveta e o vê em Serviços encerrados.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Hospital Com UTI E Nome
    Incluir Servico Do Tipo    ${uuid}    UTI    UTI Adulto
    UI Equipamentos - Abrir Unidade    ${uuid}
    Wait Until Keyword Succeeds    10s    500ms    UI Equipamentos - Painel Deve Ser    ${nome}
    Clicar Aba    Serviços
    Clicar Botao    Encerrar UTI Adulto
    Preencher Campo Da Gaveta    motivoFim    Reforma da ala
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Serviço encerrado
    Esperar Texto Aparecer    Serviços encerrados
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Cadastrar Tipo De Servico
    [Documentation]    Na aba Tipos de serviço, vê a carga e cadastra um tipo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    UI Modelo Operacional - Abrir
    Click    css=#tab-tipos
    Esperar Texto Aparecer    Centro cirúrgico
    Clicar Botao    Novo tipo de serviço
    Preencher Campo Da Gaveta    codigo    UI_${sufixo}
    Preencher Campo Da Gaveta    nome    Banco de leite ${sufixo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Tipo de serviço cadastrado
    UI Modelo Operacional - Buscar    ${sufixo}
    Esperar Texto Aparecer    Banco de leite ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

