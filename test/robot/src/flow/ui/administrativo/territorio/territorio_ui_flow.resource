*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/territorio/territorio_ui_pom.resource
Resource    ../../../territorio/territorio_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O território, as equipes e os ACS de partida são criados pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Territorio - Cadastrar Area Com Equipe
    [Documentation]    Cadastra a área pela gaveta e atribui a equipe responsável.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${territorio}    ${sufixo}=    Seed Territorio APS
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    UI Territorio - Abrir
    Clicar Botao    Nova área
    Escolher Opcao Da Gaveta    territorioId    ${territorio}
    Preencher Campo Da Gaveta    codigo    A${sufixo}
    Preencher Campo Da Gaveta    nome    Área UI ${sufixo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Área cadastrada
    UI Territorio - Detalhe Deve Mostrar    Área sem equipe responsável
    Clicar Botao    Atribuir equipe
    Escolher Opcao Da Gaveta    equipeId    ${equipe}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Equipe atribuída
    UI Territorio - Detalhe Deve Mostrar    ${nome}
    ${daEquipe}=    GET Territorio Da Equipe    ${equipe}
    Should Be Equal    ${daEquipe.json()}[area][codigo]    A${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Microarea Com ACS
    [Documentation]    Cadastra a microárea e atribui o ACS da equipe pela tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${acs}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${resp}=    Create Dictionary    equipeId=${equipe}
    POST Responsabilidade Territorial    ${area}    ${resp}
    UI Territorio - Abrir Area    ${area}
    UI Territorio - Detalhe Deve Mostrar    ${nome}
    Clicar Botao    Nova microárea
    Preencher Campo Da Gaveta    codigo    07
    Preencher Campo Da Gaveta    populacao    800
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Microárea cadastrada
    UI Territorio - Detalhe Deve Mostrar    População de referência acima de 750 pessoas (CNES)
    Clicar Botao    Atribuir ACS à microárea 07
    Escolher Opcao Da Gaveta    membroId    ${acs}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    ACS atribuído
    ${detalhe}=    GET Area Territorial    ${area}
    Should Be Equal    ${detalhe.json()}[area][microareas][0][acs][membroId]    ${acs}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Trocar ACS Com Motivo
    [Documentation]    Troca o ACS da microárea: sem motivo, a mensagem; com motivo, o histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${micro}=    Seed Microarea    ${area}    07
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${acs1}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${acs2}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${resp}=    Create Dictionary    equipeId=${equipe}
    POST Responsabilidade Territorial    ${area}    ${resp}
    ${primeiro}=    Create Dictionary    membroId=${acs1}
    POST Atribuicao Microarea    ${micro}    ${primeiro}
    UI Territorio - Abrir Area    ${area}
    UI Territorio - Detalhe Deve Mostrar    ${nome}
    Clicar Botao    Trocar ACS da microárea 07
    Escolher Opcao Da Gaveta    membroId    ${acs2}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivo
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivo    Redivisão da área
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    ACS atribuído
    UI Territorio - Detalhe Deve Mostrar    Redivisão da área
    ${detalhe}=    GET Area Territorial    ${area}
    Length Should Be    ${detalhe.json()}[atribuicoes]    2
    Should Be Equal    ${detalhe.json()}[area][microareas][0][acs][membroId]    ${acs2}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Mapa Mostra As Microareas
    [Documentation]    O mapa da área mostra as microáreas; a sem ACS tem o nome que diz isso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${g1}=    Quadrado GeoJSON    -47.0    -22.0    0.01
    ${g2}=    Quadrado GeoJSON    -46.99    -22.0    0.01
    ${m07}=    Seed Microarea    ${area}    07    ${g1}
    Seed Microarea    ${area}    08    ${g2}
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${acs}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${resp}=    Create Dictionary    equipeId=${equipe}
    POST Responsabilidade Territorial    ${area}    ${resp}
    ${atrib}=    Create Dictionary    membroId=${acs}
    POST Atribuicao Microarea    ${m07}    ${atrib}
    UI Mapa - Sem Fundo
    UI Territorio - Abrir Area    ${area}
    Wait For Elements State    css=path[data-nome="Microárea 08, sem ACS"]    visible
    Wait For Elements State    css=path[data-nome^="Microárea 07, ACS "]    visible
    Wait For Elements State    css=path[data-nome="Área 01"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Desenhar Microarea No Mapa
    [Documentation]    Desenha a microárea com o retângulo dentro da área e salva.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    Seed Microarea    ${area}    07
    UI Mapa - Sem Fundo
    UI Territorio - Abrir Area    ${area}
    UI Mapa - Caixa Do Poligono    Área 01
    Clicar Botao    Desenhar microárea 07
    Wait For Elements State    css=.button-container[title="Desenhar Retângulo"]    visible
    Sleep    1s
    ${caixa}=    UI Mapa - Caixa Do Poligono    Área 01
    ${x1}=    Evaluate    ${caixa}[x] + ${caixa}[width] * 0.2
    ${y1}=    Evaluate    ${caixa}[y] + ${caixa}[height] * 0.2
    ${x2}=    Evaluate    ${caixa}[x] + ${caixa}[width] * 0.6
    ${y2}=    Evaluate    ${caixa}[y] + ${caixa}[height] * 0.6
    UI Mapa - Desenhar Retangulo    ${x1}    ${y1}    ${x2}    ${y2}
    Esperar Texto Aparecer    ha desenhados
    Clicar Botao    Salvar desenho
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Desenho salvo
    ${detalhe}=    GET Area Territorial    ${area}
    Should Not Be Equal    ${detalhe.json()}[area][microareas][0][geometria]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Desenho Fora Da Area
    [Documentation]    O retângulo que sai da área mostra o aviso e o backend recusa.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    Seed Microarea    ${area}    07
    UI Mapa - Sem Fundo
    UI Territorio - Abrir Area    ${area}
    UI Mapa - Caixa Do Poligono    Área 01
    Clicar Botao    Desenhar microárea 07
    Wait For Elements State    css=.button-container[title="Desenhar Retângulo"]    visible
    Sleep    1s
    ${caixa}=    UI Mapa - Caixa Do Poligono    Área 01
    ${x1}=    Evaluate    ${caixa}[x] + ${caixa}[width] * 0.6
    ${y1}=    Evaluate    ${caixa}[y] + ${caixa}[height] * 0.6
    ${x2}=    Evaluate    ${caixa}[x] + ${caixa}[width] * 1.15
    ${y2}=    Evaluate    ${caixa}[y] + ${caixa}[height] * 1.1
    UI Mapa - Desenhar Retangulo    ${x1}    ${y1}    ${x2}    ${y2}
    Esperar Texto Aparecer    Parte do desenho sai do contorno da área 01
    Clicar Botao    Salvar desenho
    Esperar Texto Aparecer    sai do contorno da área 01.
    ${detalhe}=    GET Area Territorial    ${area}
    Should Be Equal    ${detalhe.json()}[area][microareas][0][geometria]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nome Da Unidade No Mapa
    [Documentation]    Devolve o nome da unidade pela lista de unidades do mapa.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Unidades No Mapa
    ${nomes}=    Evaluate    [u["nome"] for u in $lista.json() if u["uuid"] == $uuid]
    RETURN    ${nomes}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Mapa Da Rede
    [Documentation]    Na visão Mapa, a unidade com coordenadas aparece como pino; o clique abre a ficha.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    # Coordenada aleatória: execuções anteriores deixam unidades no mapa, e pinos no mesmo ponto se cobrem.
    ${lat}=    Evaluate    round(-20 - random.random() * 3, 5)    modules=random
    ${lng}=    Evaluate    round(-45 - random.random() * 5, 5)    modules=random
    ${coord}=    Create Dictionary    latitude=${lat}    longitude=${lng}
    ${r}=    PUT Coordenadas Unidade    ${unidadeId}    ${coord}
    Should Be Equal As Integers    ${r.status_code}    200
    ${nome}=    Nome Da Unidade No Mapa    ${unidadeId}
    UI Mapa - Sem Fundo
    Ir Para Rota Do Frontend    /administrativo/equipamentos
    Clicar Botao    Mapa
    UI Mapa - Pino Deve Aparecer    ${nome}
    Click    css=.leaflet-marker-icon[title="${nome}"]
    Wait For Elements State    css=aside[aria-label="Detalhes da unidade"] >> text=${nome}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Equipamentos - Marcar Localizacao
    [Documentation]    Marca a localização da unidade com um clique no minimapa e salva.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    UI Mapa - Sem Fundo
    Ir Para Rota Do Frontend    /administrativo/equipamentos?unidade=${unidadeId}
    Esperar Texto Aparecer    Sem coordenadas
    Clicar Botao    Marcar no mapa
    UI Mapa - Clicar No Centro
    Wait Until Keyword Succeeds    10s    500ms    UI Mapa - Campo Deve Ter Valor    f-latitude
    Clicar Botao    Salvar localização
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Localização salva
    ${lista}=    GET Unidades No Mapa
    ${salva}=    Evaluate    [u["latitude"] for u in $lista.json() if u["uuid"] == $unidadeId][0]
    Should Not Be Equal    ${salva}    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
