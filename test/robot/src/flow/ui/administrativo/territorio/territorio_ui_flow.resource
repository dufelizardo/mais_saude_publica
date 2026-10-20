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

