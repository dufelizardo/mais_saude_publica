*** Settings ***
Resource    ../../../pom/ui/vigilancia/vigilancia_ui_pom.resource
Resource    ../../vigilancia/vigilancia_flow.resource
Resource    ../../vigilancia/vigilancia_contatos_flow.resource
Resource    ../../vigilancia/vigilancia_surto_flow.resource
Resource    ../../vigilancia/comunicacao_violencia_flow.resource
Resource    ../../vigilancia/saude_trabalhador_flow.resource
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

UI Vigilancia - Comunicar Violencia
    [Documentation]    A notificação de violência contra a mulher aparece nas comunicações pendentes; pelo detalhe, a comunicação à delegacia (ADR-0201).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${nome}=    Seed Paciente Com Idade    30    FEMININO
    ${n}=    Notificar Violencia    ${paciente}    ${unidade}
    ${delegacia}=    Seed Instituicao Externa    Delegacia Robot UI    SEGURANCA_PUBLICA    SEGURANCA_PUBLICA
    UI Vigilancia - Abrir
    Click    css=[aria-label="Comunicações obrigatórias pendentes"] button[aria-label="Abrir notificação de ${nome}"]
    Wait For Elements State    css=[aria-label="Comunicação obrigatória"] >> text=Autoridade policial    visible
    Select Options By    id=f-cv-inst    value    ${delegacia}
    Click    css=[aria-label="Comunicação obrigatória"] button >> text=Comunicar
    Wait For Elements State    css=[aria-label="Comunicação obrigatória"] >> text=Feita    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Notificar Acidente De Trabalho
    [Documentation]    O acidente grave pede a data e o bloco do trabalho; preenchido, a notificação mostra o bloco (ADR-0207).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${grave}=    Id Do Agravo    ACIDENTE_TRABALHO_GRAVE
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    UI Vigilancia - Abrir
    Clicar Botao    Notificar
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Click    css=app-drawer [role="listbox"] >> role=option[name="${nomePaciente}"]
    Escolher Opcao Da Gaveta    agravo    ${grave}
    Escolher Opcao Da Gaveta    unidadeNotificante    ${unidade}
    Wait For Elements State    id=f-ocupacaoCbo    visible
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Informe a data do acidente.    visible
    Preencher Campo Da Gaveta    dataPrimeirosSintomas    ${ontem}
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=O código da CBO tem seis dígitos (ex.: 7152-10).    visible
    Preencher Campo Da Gaveta    ocupacaoCbo    7152-10
    Preencher Campo Da Gaveta    ocupacaoDescricao    Pedreiro
    Escolher Opcao Da Gaveta    situacaoTrabalho    EMPREGADO_REGISTRADO
    Preencher Campo Da Gaveta    empregadorNome    Construtora Robot UI
    Escolher Opcao Da Gaveta    tipoAcidente    TIPICO
    Escolher Opcao Da Gaveta    cat    NAO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Notificação registrada
    Wait For Elements State    css=[aria-label="Bloco do trabalho"] >> text=Pedreiro    visible
    Wait For Elements State    css=[aria-label="Bloco do trabalho"] >> text=Construtora Robot UI    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Acompanhamento Sorologico
    [Documentation]    Na aba Saúde do trabalhador, a etapa de 6 semanas vencida; pelo detalhe, a etapa registrada (ADR-0207).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${bloco}=    Bloco Do Trabalho    ocupacaoCbo=322205    ocupacaoDescricao=Técnico de enfermagem    situacaoTrabalho=SERVIDOR_ESTATUTARIO
    ${respostas}=    Respostas Material Biologico
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO    50    ${bloco}    ${respostas}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    UI Vigilancia - Abrir
    Click    css=[role="tab"]:has-text("Saúde do trabalhador")
    Wait For Elements State    css=[aria-label="Acompanhamentos sorológicos a fazer"] >> text=${nomePaciente} · 6 semanas    visible
    Click    css=[aria-label="Acompanhamentos sorológicos a fazer"] button[aria-label="Abrir notificação de ${nomePaciente}"]
    Clicar Botao    Registrar etapa 6 semanas
    Click    css=[aria-label="Acompanhamento sorológico"] button.btn
    Wait For Elements State    css=[aria-label="Acompanhamento sorológico"] >> text=Feita    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Notificar Acidente Do Servidor
    [Documentation]    O acidente do servidor no RH aparece sem notificação; pelo botão, a notificação já ligada a ele (ADR-0207).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${cpf}=    Seed A Profissional Unit
    ${prof}=    Call GET Get Profissional By Cpf    ${cpf}
    ${matricula}=    Set Variable    ${prof.json()}[matricula]
    ${nomeProf}=    Set Variable    ${prof.json()}[nome]
    ${suffix}=    Generate Random String    8    [NUMBERS]
    ${pac}=    Build Paciente Payload    Robot Servidor UI ${suffix}    ${cpf}
    ${r}=    POST Create Paciente    ${pac}
    Should Be Equal As Integers    ${r.status_code}    201
    ${quando}=    Evaluate    (datetime.datetime.now() - datetime.timedelta(days=1)).replace(microsecond=0).isoformat()    modules=datetime
    ${acidente}=    Create Dictionary    matriculaProfissional=${matricula}    dataHora=${quando}    descricao=Queda da escada    catEmitida=${True}    diasAfastamento=${2}
    ${r}=    POST Acidente De Trabalho Do RH    ${acidente}
    Should Be Equal As Integers    ${r.status_code}    201
    UI Vigilancia - Abrir
    Click    css=[role="tab"]:has-text("Saúde do trabalhador")
    Clicar Botao    Notificar acidente de ${nomeProf}
    Wait For Elements State    id=f-ocupacaoCbo    visible
    Escolher Opcao Da Gaveta    unidadeNotificante    ${unidade}
    Preencher Campo Da Gaveta    ocupacaoCbo    4110-05
    Preencher Campo Da Gaveta    ocupacaoDescricao    Auxiliar administrativo
    Escolher Opcao Da Gaveta    situacaoTrabalho    SERVIDOR_ESTATUTARIO
    Escolher Opcao Da Gaveta    tipoAcidente    TIPICO
    Wait For Elements State    css=app-drawer >> text=Ligada ao acidente do servidor registrado no RH (a CAT continua com o RH).    visible
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Notificação registrada
    Wait For Elements State    css=[aria-label="Bloco do trabalho"] >> text=Acidente registrado no RH em    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Investigar O Trabalho
    [Documentation]    Pela tela: a visita, o nexo confirmado, a medida recomendada e verificada (ADR-0208).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${n}=    Notificar Acidente Grave    ${paciente}    ${unidade}
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${prazo}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=10)).isoformat()    modules=datetime
    UI Vigilancia - Abrir
    Click    css=[role="tab"]:has-text("Saúde do trabalhador")
    Clicar Botao    Abrir notificação: ${nomePaciente} Acidente de trabalho grave, fatal ou com crianças e adolescentes
    Fill Text    id=f-inv-visita    ${hoje}
    Fill Text    id=f-inv-achados    Andaime sem guarda-corpo
    Clicar Botao    Iniciar a investigação
    Wait For Elements State    css=[aria-label="Visita ao local de trabalho"] >> text=Nexo em análise    visible
    Select Options By    id=f-inv-nexo    value    CONFIRMADO
    Fill Text    id=f-inv-justificativa    Queda do andaime
    Clicar Botao    Concluir o nexo
    Wait For Elements State    css=[aria-label="Visita ao local de trabalho"] >> text=Nexo confirmado    visible
    Select Options By    id=f-med-tipo    value    PROTECAO_COLETIVA
    Fill Text    id=f-med-prazo    ${prazo}
    Fill Text    id=f-med-descricao    Instalar guarda-corpo
    Clicar Botao    Recomendar a medida
    Wait For Elements State    css=[aria-label="Medidas do trabalho"] >> text=Recomendada    visible
    Clicar Botao    Verificar medida: Instalar guarda-corpo
    Click    css=[aria-label="Medidas do trabalho"] button.btn
    Wait For Elements State    css=[aria-label="Medidas do trabalho"] >> text=Cumprida    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Risco Ocupacional
    [Documentation]    Na aba Saúde do trabalhador: registrar o risco do empregador e marcá-lo como controlado (ADR-0208).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    UI Vigilancia - Abrir
    Click    css=[role="tab"]:has-text("Saúde do trabalhador")
    Fill Text    id=f-rn-empregador    Metalurgica Robot ${sufixo}
    Select Options By    id=f-rn-tipo    value    FISICO
    Fill Text    id=f-rn-agente    Ruído ${sufixo}
    Clicar Botao    Registrar o risco
    Wait For Elements State    css=[aria-label="Riscos ocupacionais"] >> text=Metalurgica Robot ${sufixo} · Físico · Ruído ${sufixo}    visible
    Clicar Botao    Marcar como controlado: Ruído ${sufixo}
    Fill Text    id=f-risco-motivo    Enclausuramento da prensa
    Click    css=[aria-label="Riscos ocupacionais"] button.btn
    Wait For Elements State    css=[aria-label="Riscos ocupacionais"] li:has-text("Ruído ${sufixo}") >> text=Enclausuramento da prensa    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia - Trabalho Na Sala De Situacao
    [Documentation]    O acidente de trabalho da semana aparece como alerta na Sala de Situação (ADR-0208).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${n}=    Notificar Acidente Grave    ${paciente}    ${unidade}
    Ir Para Rota Do Frontend    /sala-de-situacao
    Wait For Elements State    css=[aria-label="Saúde do trabalhador na semana"]    visible
    ${texto}=    Get Text    css=[aria-label="Saúde do trabalhador na semana"]
    Should Contain    ${texto}    nos últimos 7 dias
    Should Not Contain    ${texto}    ${nomePaciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
