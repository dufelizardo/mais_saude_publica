*** Settings ***
Library     Browser
Library     String
Library     DateTime
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../comunicacao/comunicacao_flow.resource
Resource    ../../../comunicacao/avaliacao_campanha_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Comunicação (ADR-0168) e os avisos na página pública.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Comunicacao - Abrir
    [Documentation]    Abre a tela Comunicação.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/comunicacao
    Wait For Elements State    css=h1 >> text=Comunicação    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Comunicacao - Criar E Publicar Comunicado
    [Documentation]    Sem título, a mensagem; com os dados, salva o rascunho e publica.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${titulo}=    Set Variable    UI Aviso ${sufixo}
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    UI Comunicacao - Abrir
    Wait For Elements State    css=#tab-canais .cnt >> text=/[1-9]/    visible
    Clicar Botao    Novo comunicado
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    titulo
    Should Contain    ${erro}    título
    Preencher Campo Da Gaveta    titulo    ${titulo}
    Fill Text    id=f-mensagem    UBS Central em obra
    Preencher Campo Da Gaveta    fim    ${hoje}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Rascunho salvo
    Clicar Botao    Publicar ${titulo}
    Wait For Elements State    css=tr:has-text("${titulo}") >> text=No ar    visible
    Ir Para Rota Do Frontend    /
    Wait For Elements State    css=#avisos >> text=${titulo}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Comunicacao - Fluxo Do Conteudo
    [Documentation]    Cria o conteúdo, envia à revisão, aprova e publica pela tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${titulo}=    Set Variable    UI Dengue ${sufixo}
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${validade}=    Add Time To Date    ${hoje}    90 days    result_format=%Y-%m-%d
    UI Comunicacao - Abrir
    Click    css=#tab-conteudos
    Clicar Botao    Novo conteúdo
    Preencher Campo Da Gaveta    tituloConteudo    ${titulo}
    Fill Text    id=f-resumo    Como evitar focos
    Fill Text    id=f-texto    Elimine a água parada.
    Preencher Campo Da Gaveta    tema    Dengue
    Preencher Campo Da Gaveta    validade    ${validade}
    Enviar Gaveta
    Clicar Botao    Enviar à revisão ${titulo}
    Clicar Botao    Aprovar ${titulo}
    Clicar Botao    Publicar ${titulo}
    Enviar Gaveta
    Wait For Elements State    css=tr:has-text("${titulo}") >> text=Publicado    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Comunicacao - Encerrar Comunicado
    [Documentation]    Encerra um comunicado publicado: sem motivo, a mensagem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${titulo}=    Set Variable    UI Encerrar ${sufixo}
    ${payload}=    Payload Do Comunicado    ${titulo}
    ${criado}=    POST Create Comunicado    ${payload}
    POST Publicar Comunicado    ${criado.json()}[uuid]
    UI Comunicacao - Abrir
    Clicar Botao    Encerrar ${titulo}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivo
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivo    Obra concluída
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Comunicado encerrado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════


UI Comunicacao - Planejar E Iniciar Campanha
    [Documentation]    Pela aba Campanhas (ADR-0174): sem nome, a mensagem; planeja com o portal, inicia e vê na página pública.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    UI Campanha ${sufixo}
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    UI Comunicacao - Abrir
    Wait For Elements State    css=#tab-canais .cnt >> text=/[1-9]/    visible
    Click    css=#tab-campanhas
    Clicar Botao    Nova campanha
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    nomeCampanha
    Should Contain    ${erro}    nome
    Preencher Campo Da Gaveta    nomeCampanha    ${nome}
    Preencher Campo Da Gaveta    objetivoCampanha    Estimular a mamografia
    Preencher Campo Da Gaveta    fimCampanha    ${hoje}
    Check Checkbox    css=label.perm-check:has-text("Portal") >> input
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Campanha planejada
    Clicar Botao    Iniciar ${nome}
    Wait For Elements State    css=tr:has-text("${nome}") >> text=Em andamento    visible
    Ir Para Rota Do Frontend    /
    Wait For Elements State    css=#campanhas-comunicacao >> text=${nome}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════


UI Comunicacao - Avaliar Campanha
    [Documentation]    Na campanha encerrada (ADR-0196): a seção Avaliação com a meta abaixo; sem resposta, a mensagem; registra e confere na API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}=    Seed Campanha Com Metas    30    1
    ${balanco}=    Create Dictionary    balanco=Encerrada pelo Robot
    ${r}=    POST Acao Da Campanha    ${campanha}    encerramento    ${balanco}
    Should Be Equal As Integers    ${r.status_code}    200
    UI Comunicacao - Abrir
    Click    css=#tab-campanhas
    Clicar Botao    Detalhes de ${nome}
    Wait For Elements State    css=app-drawer #h-avaliacao    visible
    Wait For Elements State    css=app-drawer dl[aria-labelledby="h-avaliacao"] >> text=0 de 30 · abaixo    visible
    Clicar Botao    Avaliar campanha
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    publicoAtingido
    Should Contain    ${erro}    público-alvo
    Select Options By    id=f-publicoAtingido    value    NAO
    Preencher Campo Da Gaveta    conclusoesAvaliacao    A campanha não chegou às escolas.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Avaliação registrada
    ${d}=    GET Educacao Generico    comunicacao/campanha/${campanha}/avaliacao
    Should Be Equal    ${d.json()}[avaliacao][publicoAtingido]    NAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
