*** Settings ***
Resource    ../../../pom/ui/vigilancia/vigilancia_ui_pom.resource
Resource    ../../vigilancia/vigilancia_flow.resource
Resource    ../../vigilancia/vigilancia_contatos_flow.resource
Resource    ../../../pom/ui/administrativo/territorio/territorio_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base é criada pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Vigilancia - Notificar Dengue
    [Documentation]    A ficha da dengue aparece no formulário; sem a febre, a mensagem; com ela, a notificação.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    UI Vigilancia - Abrir
    Clicar Botao    Notificar
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Click    css=app-drawer [role="listbox"] >> role=option[name="${nomePaciente}"]
    Escolher Opcao Da Gaveta    agravo    ${dengue}
    Wait For Elements State    id=f-ficha-febre    visible
    Escolher Opcao Da Gaveta    unidadeNotificante    ${unidade}
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Responda este campo.    visible
    Escolher Opcao Da Gaveta    ficha-febre    true
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Notificação registrada
    Wait For Elements State    css=[aria-label="Respostas da ficha"] >> text=Sim    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Classificar E Encerrar
    [Documentation]    Pela gaveta da notificação: classificar como confirmado e encerrar com a evolução.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    UI Vigilancia - Abrir
    Clicar Botao    Abrir notificação: ${nomePaciente} Dengue
    Escolher Opcao Da Gaveta    classificacao    CONFIRMADO
    Clicar Botao    Classificar
    Wait For Elements State    css=app-drawer >> text=Confirmado e descartado pedem o critério.    visible
    Escolher Opcao Da Gaveta    criterio    LABORATORIAL
    Clicar Botao    Classificar
    Wait For Elements State    css=[aria-label="Classificações do caso"] >> text=Laboratorial    visible
    Escolher Opcao Da Gaveta    evolucao    CURA
    Clicar Botao    Encerrar a notificação
    Wait For Elements State    css=app-drawer >> text=Encerrada em    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Notificacao Negativa
    [Documentation]    A unidade com notificação aparece com as semanas pendentes; declarar tira a semana da lista.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    UI Vigilancia - Abrir
    Clicar Aba    Notificação negativa
    Click    css=[aria-label="Semanas pendentes"] >> role=button >> nth=0
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Notificação negativa registrada
    Wait For Elements State    css=[aria-label="Notificações negativas"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Registrar Contato
    [Documentation]    Na gaveta da notificação: sem nome, a mensagem; com o nome, o contato na lista; depois, a situação muda.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    UI Vigilancia - Abrir
    Clicar Botao    Abrir notificação: ${nomePaciente} Dengue
    Clicar Botao    Registrar contato
    Clicar Botao    Salvar contato
    Wait For Elements State    css=app-drawer >> text=Sem cadastro, informe o nome do contato.    visible
    Fill Text    id=f-contato-nome    Vizinha Robot
    Fill Text    id=f-contato-telefone    19 99999-0000
    Select Options By    id=f-contato-tipo    value    COMUNITARIO
    Clicar Botao    Salvar contato
    Wait For Elements State    css=[aria-label="Contatos do caso"] >> text=Vizinha Robot    visible
    Wait For Elements State    css=[aria-label="Contatos do caso"] >> text=Em acompanhamento    visible
    Select Options By    css=[aria-label="Situação de Vizinha Robot"]    value    LIBERADO
    Wait For Elements State    css=[aria-label="Contatos do caso"] .tag >> text=Liberado    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Notificar Contato
    [Documentation]    O contato que adoeceu: "Notificar" abre a notificação já preenchida e ligada ao caso índice.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    ${outro}    ${cpf2}    ${cns2}    ${nomeOutro}=    Seed A Paciente
    ${contato}=    Create Dictionary    pacienteId=${outro}    tipoExposicao=DOMICILIAR
    ${c}=    POST Contato    ${notificacao}    ${contato}
    Should Be Equal As Integers    ${c.status_code}    201
    UI Vigilancia - Abrir
    Clicar Botao    Abrir notificação: ${nomePaciente} Dengue
    Clicar Botao    Notificar o contato ${nomeOutro}
    Wait For Elements State    css=app-drawer >> text=Contato de ${nomePaciente}    visible
    Escolher Opcao Da Gaveta    ficha-febre    true
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Notificação registrada
    ${d}=    GET Notificacao    ${notificacao}
    Should Be Equal    ${d.json()}[contatos][0][situacao]    VIROU_CASO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Local E Mapa
    [Documentation]    Marcar o local do caso no minimapa e ver o caso no mapa da vigilância.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    UI Mapa - Sem Fundo
    UI Vigilancia - Abrir
    Clicar Botao    Abrir notificação: ${nomePaciente} Dengue
    Clicar Botao    Marcar local no mapa
    UI Mapa - Clicar No Centro
    Clicar Botao    Salvar local
    Wait For Elements State    css=app-drawer [aria-label="Local do caso"] >> text=Marcado no mapa    visible
    ${d}=    GET Notificacao    ${notificacao}
    Should Be Equal    ${d.json()}[origemLocal]    MARCADO
    Click    css=app-drawer button[aria-label="Fechar"]
    Clicar Aba    Mapa
    Wait For Elements State    css=[aria-label="Resumo do mapa"]    visible
    Wait For Elements State    css=[aria-label="Casos por semana epidemiológica"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
