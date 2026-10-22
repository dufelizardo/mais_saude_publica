*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/territorio/territorio_ui_pom.resource
Resource    ../../../territorio/territorio_flow.resource
Resource    ../../../territorio/cadastro_territorial_flow.resource
Resource    ../../../territorio/adscricao_flow.resource
Resource    ../../../../pom/ui/assistencia/pacientes/pacientes_ui_pom.resource
Resource    ../../../../pom/ui/assistencia/programas/programas_ui_pom.resource
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

UI Territorio - Cadastrar Imovel E Domicilio
    [Documentation]    Imóvel pela gaveta, na microárea, e o domicílio com as condições.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    UI Mapa - Sem Fundo
    UI Territorio - Abrir Area    ${area}
    Clicar Botao    Novo imóvel
    Escolher Opcao Da Gaveta    microareaId    ${micro}
    Preencher Campo Da Gaveta    logradouro    Rua das Flores
    Preencher Campo Da Gaveta    numero    45
    Preencher Campo Da Gaveta    bairro    Jardim
    Preencher Campo Da Gaveta    cep    13000-000
    Preencher Campo Da Gaveta    cidade    Campinas
    Preencher Campo Da Gaveta    estado    SP
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Imóvel cadastrado
    Wait For Elements State    id=ficha-imovel    visible
    Clicar Botao    Novo domicílio
    Preencher Campo Da Gaveta    identificacao    Casa da frente
    Escolher Opcao Da Gaveta    situacao    ALUGADO
    Escolher Opcao Da Gaveta    agua    REDE_ENCANADA
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Domicílio cadastrado
    Esperar Texto Aparecer    Casa da frente
    ${lista}=    GET Imoveis Da Area    ${area}
    Length Should Be    ${lista.json()}    1
    Should Be Equal As Integers    ${lista.json()}[0][domicilios]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Cadastrar Familia
    [Documentation]    Família com o responsável e uma pessoa incluída, pela tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${ana}    ${c1}    ${s1}    ${nomeAna}=    Seed A Paciente
    ${bia}    ${c2}    ${s2}    ${nomeBia}=    Seed A Paciente
    UI Mapa - Sem Fundo
    UI Territorio - Abrir Area    ${area}
    UI Territorio - Abrir Primeiro Imovel
    Clicar Botao    Nova família
    UI Territorio - Escolher Paciente    ${nomeAna}    ${ana}
    Escolher Opcao Da Gaveta    renda    ATE_DOIS_SM
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Família cadastrada
    Clicar Botao    Incluir pessoa
    UI Territorio - Escolher Paciente    ${nomeBia}    ${bia}
    Escolher Opcao Da Gaveta    parentesco    CONJUGE
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Pessoa incluída
    ${daBia}=    GET Familia Do Paciente    ${bia}
    Should Be Equal    ${daBia.json()}[familia][responsavelId]    ${ana}
    Length Should Be    ${daBia.json()}[familia][membros]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Endereco E Saida
    [Documentation]    Leva o endereço do imóvel ao cadastro e registra a saída com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${ana}    ${c1}    ${s1}    ${nomeAna}=    Seed A Paciente
    ${payload}=    Create Dictionary    domicilioId=${domicilio}    responsavelId=${ana}
    POST Familia    ${payload}
    UI Mapa - Sem Fundo
    UI Territorio - Abrir Area    ${area}
    UI Territorio - Abrir Primeiro Imovel
    Clicar Botao    Usar o endereço do imóvel no cadastro de ${nomeAna}
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Endereço atualizado
    Clicar Botao    Saída de ${nomeAna}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivo
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivo    Mudou de cidade
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Saída registrada
    Esperar Texto Aparecer    Nenhuma família morando aqui.
    ${daAna}=    GET Familia Do Paciente    ${ana}
    Should Be Equal    ${daAna.json()}[familia]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Equipe De Referencia Na Ficha
    [Documentation]    A ficha do paciente mostra a equipe de referência pelo domicílio e a microárea.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nome}
    UI Pacientes - Abrir Paciente    ${nome}
    Wait For Elements State    css=[aria-label="Equipe de referência"] >> text=Pelo domicílio    visible
    Wait For Elements State    css=[aria-label="Equipe de referência"] >> text=microárea 07    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Adscricao Manual Na Ficha
    [Documentation]    Adscreve o paciente sem domicílio à mão, com motivo, pela ficha.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${equipe}    ${nomeEquipe}    ${unidade}=    Seed Uma Equipe
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nome}
    UI Pacientes - Abrir Paciente    ${nome}
    Clicar Botao    Adscrever manualmente
    Wait For Elements State    css=#f-equipeId option[value="${equipe}"]    attached
    Escolher Opcao Da Gaveta    equipeId    ${equipe}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivo
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivo    Situação de rua
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Equipe de referência"] .tag >> text="Manual"    visible
    ${a}=    GET Adscricao Do Paciente    ${paciente}
    Should Be Equal    ${a.json()}[atual][origem]    MANUAL
    Should Be Equal    ${a.json()}[atual][equipeId]    ${equipe}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Elegiveis E Inscricao
    [Documentation]    No programa, os elegíveis da equipe e a inscrição já preenchida.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${programa}=    Seed Programa Adulto Na Unidade    ${unidade}
    UI Programas - Abrir Programa    ${programa}
    Wait For Elements State    css=select[aria-label="Equipe dos elegíveis"] option[value="${equipe}"]    attached
    Select Options By    css=select[aria-label="Equipe dos elegíveis"]    value    ${equipe}
    Clicar Botao    Inscrever ${nome}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente inscrito
    Esperar Texto Aparecer    Nenhum elegível fora do programa.
    ${params}=    Create Dictionary    equipeId=${equipe}
    ${r}=    GET Elegiveis Do Programa    ${programa}    ${params}
    Length Should Be    ${r.json()}[elegiveis]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
