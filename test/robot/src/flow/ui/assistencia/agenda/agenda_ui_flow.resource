*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/agenda/agenda_ui_pom.resource
Resource    ../../../agendamento/falta_agendamento/falta_agendamento_flow.resource
Resource    ../../../../pom/agenda/bloqueio_agenda/bloqueio_agenda_pom.resource
Resource    ../../../equipamentos/rede_unidades/rede_unidades_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Profissional, unidade, bloco e paciente são criados pela API; a ação sob teste é feita pela tela (ADR-0092).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Agenda - Marcar Na Vaga
    [Documentation]    Clica numa vaga livre, marca o paciente pela gaveta e vê o aviso e o nome no calendário.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    UI Agenda - Abrir    ${matricula}    ${unidadeId}    ${segunda}
    UI Agenda - Clicar Vaga    08:30
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Nova marcação
    Preencher Campo Da Gaveta    buscaPaciente    ${nome}
    Escolher Opcao Da Gaveta    pacienteId    ${pacienteId}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Marcação feita
    Esperar Texto Aparecer    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Agenda - Marcar Encaixe
    [Documentation]    Marca um encaixe fora das vagas pelo botão Nova marcação.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    UI Agenda - Abrir    ${matricula}    ${unidadeId}    ${segunda}
    Clicar Botao    Nova marcação
    Preencher Campo Da Gaveta    buscaPaciente    ${nome}
    Escolher Opcao Da Gaveta    pacienteId    ${pacienteId}
    Preencher Campo Da Gaveta    dataHora    ${segunda}T08:10
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Encaixe marcado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Agenda - Registrar Falta
    [Documentation]    Abre a marcação no calendário e registra a falta.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${payload}=    Build Agendamento Na Agenda Payload    ${pacienteId}    ${matricula}    ${unidadeId}    ${segunda}T09:00:00
    ${criado}=    POST Create Agendamento Na Agenda    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    UI Agenda - Abrir    ${matricula}    ${unidadeId}    ${segunda}
    UI Agenda - Clicar Marcacao Do Paciente    ${nome}
    Clicar Botao    Registrar falta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Falta registrada
    ${lista}=    GET Agendamentos Do Paciente    ${pacienteId}
    Should Be Equal    ${lista.json()}[0][status]    FALTOU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Agenda - Criar Bloco Pela Gaveta
    [Documentation]    Cria um bloco pelo painel e vê as vagas aparecerem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${segunda}=    Evaluate    str(datetime.date.today() + datetime.timedelta(days=(7 - datetime.date.today().weekday()) or 7))    modules=datetime
    UI Agenda - Abrir    ${matricula}    ${unidadeId}    ${segunda}
    Esperar Texto Aparecer    Sem vagas neste período
    Clicar Botao    + Bloco
    Escolher Opcao Da Gaveta    diaSemana    MONDAY
    Preencher Campo Da Gaveta    horaInicio    08:00
    Preencher Campo Da Gaveta    horaFim    09:00
    Preencher Campo Da Gaveta    duracaoMinutos    30
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Bloco criado
    Esperar Texto Aparecer    Vaga livre
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Agenda - Bloquear Pela Gaveta
    [Documentation]    Bloqueia uma hora pelo painel e vê o bloqueio no calendário.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    UI Agenda - Abrir    ${matricula}    ${unidadeId}    ${segunda}
    Clicar Botao    + Bloqueio
    Preencher Campo Da Gaveta    inicio    ${segunda}T08:00
    Preencher Campo Da Gaveta    fim    ${segunda}T09:00
    Escolher Opcao Da Gaveta    motivo    CAPACITACAO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Agenda bloqueada
    Esperar Texto Aparecer    Capacitação
    ${vagas}=    GET Vagas Agenda    ${matricula}    ${unidadeId}    ${segunda}
    Length Should Be    ${vagas.json()}    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Agenda - Sem Filtro Pede Profissional E Unidade
    [Documentation]    Sem profissional e unidade, a tela pede os dois.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Agenda - Abrir Sem Filtro
    Esperar Texto Aparecer    Escolha o profissional e a unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Agenda - Unidade Em Obra Avisa E Fecha
    [Documentation]    Com a unidade em obra, a agenda avisa e não mostra vagas (ADR-0101).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${payload}=    Create Dictionary    situacao=EM_OBRA    motivo=Reforma do telhado
    ${response}=    POST Situacao Unidade    ${unidadeId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    UI Agenda - Abrir    ${matricula}    ${unidadeId}    ${segunda}    semana
    Esperar Texto Aparecer    Unidade em obra: sem vagas nem marcação
    Esperar Texto Aparecer    Reforma do telhado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
