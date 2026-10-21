*** Settings ***
Resource    ../../../pom/ui/vigilancia/vigilancia_ui_pom.resource
Resource    ../../vigilancia/vigilancia_flow.resource
Resource    ../../vigilancia/vigilancia_contatos_flow.resource
Resource    ../../vigilancia/vigilancia_surto_flow.resource
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

UI Vigilancia - Abrir Surto
    [Documentation]    Aba Surtos: abrir o surto escolhendo os casos do agravo na lista.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${n1}=    GET Notificacao    ${c1}
    ${n2}=    GET Notificacao    ${c2}
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${sufixo}=    Generate Random String    5    [UPPER]
    UI Vigilancia - Abrir
    Click    css=[role="tab"]:has-text("Surtos")
    Clicar Botao    Abrir surto
    Select Options By    id=f-surto-agravo    value    ${sarampo}
    Select Options By    id=f-surto-unidade    value    ${unidade}
    Fill Text    id=f-surto-nome    Sarampo Robot ${sufixo}
    Check Checkbox    css=[aria-label="Casos do agravo"] label:has-text("${n1.json()}[pacienteNome]") >> input
    Check Checkbox    css=[aria-label="Casos do agravo"] label:has-text("${n2.json()}[pacienteNome]") >> input
    Click    css=app-drawer footer >> role=button[name="Abrir surto"]
    Wait For Elements State    css=[aria-label="Casos do surto"] >> text=${n1.json()}[pacienteNome]    visible
    Wait For Elements State    css=[aria-label="Casos do surto"] >> text=${n2.json()}[pacienteNome]    visible
    Wait For Elements State    css=app-drawer .tag >> text=Suspeito    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Ciclo Do Surto
    [Documentation]    Na gaveta do surto: investigar, confirmar, registrar e concluir a medida e encerrar com a conclusão.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${c1}    ${c2}=    Seed Dois Casos De Sarampo
    ${sufixo}=    Generate Random String    5    [UPPER]
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${ids}=    Create List    ${c1}
    ${payload}=    Create Dictionary    agravoId=${sarampo}    unidadeId=${unidade}    nome=Surto Robot ${sufixo}    tipo=COMUNITARIO    inicio=${hoje}    notificacoesIds=${ids}
    ${s}=    POST Surto    ${payload}
    Should Be Equal As Integers    ${s.status_code}    201
    UI Vigilancia - Abrir
    Click    css=[role="tab"]:has-text("Surtos")
    Clicar Botao    Abrir surto: Surto Robot ${sufixo}
    Clicar Botao    Investigar
    Wait For Elements State    css=app-drawer .tag >> text=Em investigação    visible
    Clicar Botao    Confirmar
    Wait For Elements State    css=app-drawer .tag >> text=Confirmado    visible
    Clicar Botao    Registrar medida
    Fill Text    id=f-medida-descricao    Vacinar os contatos
    Clicar Botao    Salvar medida
    Wait For Elements State    css=[aria-label="Medidas de controle"] >> text=Vacinar os contatos    visible
    Clicar Botao    Concluir Bloqueio vacinal
    Fill Text    css=[aria-label="Resultado de Bloqueio vacinal"]    120 doses
    Click    css=[aria-label="Medidas de controle"] >> role=button[name="Concluir"]
    Wait For Elements State    css=[aria-label="Medidas de controle"] .tag >> text=Concluída    visible
    Clicar Botao    Encerrar…
    Fill Text    id=f-surto-conclusao    Transmissão interrompida.
    Clicar Botao    Encerrar o surto
    Wait For Elements State    css=app-drawer .tag >> text=Encerrado    visible
    Wait For Elements State    css=[aria-label="Linha do tempo do surto"] >> text=Situação    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Medida Do Caso
    [Documentation]    Na gaveta da notificação: registrar a medida de controle do caso e vê-la na linha do tempo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${sarampo}    unidadeId=${unidade}    origem=ATENDIMENTO
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    UI Vigilancia - Abrir
    Clicar Botao    Abrir notificação: ${nomePaciente} Sarampo
    Clicar Botao    Registrar medida
    Select Options By    id=f-medida-tipo    value    ISOLAMENTO
    Fill Text    id=f-medida-descricao    Isolamento domiciliar
    Clicar Botao    Salvar medida
    Wait For Elements State    css=[aria-label="Medidas de controle"] >> text=Isolamento domiciliar    visible
    Wait For Elements State    css=[aria-label="Linha do tempo do caso"] >> text=Medida    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
