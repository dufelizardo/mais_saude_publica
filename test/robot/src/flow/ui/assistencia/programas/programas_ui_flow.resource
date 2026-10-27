*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/programas/programas_ui_pom.resource
Resource    ../../../programas/programa/programa_flow.resource
Resource    ../../../programas/acompanhamento/acompanhamento_flow.resource
Resource    ../../../programas/acao/acao_programatica_flow.resource
Resource    ../../../programas/campanha/campanha_flow.resource
Resource    ../../../programas/indicador/indicador_programatico_flow.resource
Resource    ../../../../pom/ui/assistencia/pacientes/pacientes_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os programas e as unidades de apoio são criados pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Programas - Detalhe Deve Ser
    [Documentation]    Confere o nome do programa no detalhe.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${titulo}=    UI Programas - Ler Titulo Do Detalhe
    Should Be Equal    ${titulo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Ver Cartao E Detalhe
    [Documentation]    A grade mostra os programas da carga; clicar no cartão abre o detalhe com o público-alvo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Programas - Abrir
    UI Programas - Clicar No Cartao    Saúde do Idoso
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    Saúde do Idoso
    Esperar Texto Aparecer    60 anos ou mais
    Esperar Texto Aparecer    Unidades e equipes participantes
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Cadastrar Programa
    [Documentation]    Cadastra um programa pela gaveta e vê o aviso e o detalhe.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${nome}=    Set Variable    UI Programa ${sufixo}
    ${categoria}=    Id Da Categoria    Ciclos de vida
    UI Programas - Abrir
    Clicar Botao    Novo programa
    Preencher Campo Da Gaveta    codigo    UI_${sufixo}
    Preencher Campo Da Gaveta    nome    ${nome}
    Escolher Opcao Da Gaveta    categoriaId    ${categoria}
    Preencher Campo Da Gaveta    idadeMinima    10
    Preencher Campo Da Gaveta    idadeMaxima    19
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Programa cadastrado
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Esperar Texto Aparecer    De 10 a 19 anos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Gaveta Recusa Dados Invalidos
    [Documentation]    Código com espaço, nome vazio e condição inválida mostram as mensagens dos campos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Programas - Abrir
    Clicar Botao    Novo programa
    Preencher Campo Da Gaveta    codigo    COM ESPACO
    Preencher Campo Da Gaveta    condicoes    hipertensao
    Enviar Gaveta
    ${erroCodigo}=    Ler Erro Do Campo Da Gaveta    codigo
    Should Contain    ${erroCodigo}    sublinhado
    ${erroNome}=    Ler Erro Do Campo Da Gaveta    nome
    Should Contain    ${erroNome}    nome
    ${erroCondicao}=    Ler Erro Do Campo Da Gaveta    condicoes
    Should Contain    ${erroCondicao}    CID-10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Incluir Unidade
    [Documentation]    Inclui uma unidade participante pela gaveta e a vê no detalhe.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Incluir unidade
    Escolher Opcao Da Gaveta    unidadeId    ${unidade}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Unidade incluída
    Wait For Elements State    css=[aria-label="Participações vigentes"] >> text=${nomeUnidade}    visible
    ${detalhe}=    GET Programa    ${programa}
    Length Should Be    ${detalhe.json()}[participacoes]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Encerrar Participacao
    [Documentation]    Encerra a participação pela gaveta: sem motivo, a mensagem; com motivo, vai para o histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    POST Participacao Programa    ${programa}    ${payload}
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Encerrar ${nomeUnidade}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoFim
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoFim    Programa passou a outra unidade
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Participação encerrada
    ${detalhe}=    GET Programa    ${programa}
    Length Should Be    ${detalhe.json()}[participacoes]    0
    Should Be Equal    ${detalhe.json()}[participacoesEncerradas][0][motivoFim]    Programa passou a outra unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Consulta Publica Na Landing
    [Documentation]    A landing page mostra os programas ativos e onde são oferecidos, sem login.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    POST Participacao Programa    ${programa}    ${payload}
    UI Programas - Abrir Landing
    Esperar Texto Aparecer    Cuidado organizado para cada fase da vida
    Wait For Elements State    css=#programas article[aria-label="${nome}"]    visible
    ${texto}=    Get Text    css=#programas article[aria-label="${nome}"]
    Should Contain    ${texto}    ${nomeUnidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Inscrever Paciente
    [Documentation]    Inscreve um paciente pela gaveta (busca pelo nome) e o vê na lista de acompanhados.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}    ${unidade}=    Seed Programa Com Unidade
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Inscrever paciente
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Click    css=app-drawer [role="listbox"] >> role=option[name="${nomePaciente}"]
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente inscrito
    Wait For Elements State    css=table[aria-label="Pacientes acompanhados"] >> text=${nomePaciente}    visible
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    Should Be Equal As Integers    ${inscritos.json()}[inscritos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Marcar Faltoso
    [Documentation]    Marca o acompanhado como faltoso pela lista, com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Marcar faltoso: ${nomePaciente}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoSituacao
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoSituacao    Não compareceu às três últimas consultas
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Marcado como faltoso
    ${depois}=    GET Inscritos Do Programa    ${programa}
    Should Be Equal As Integers    ${depois.json()}[faltosos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Aba Programas Do Paciente
    [Documentation]    A aba Programas de Pacientes mostra o programa em que o paciente é acompanhado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nomePaciente}
    UI Pacientes - Abrir Paciente    ${nomePaciente}
    Clicar Aba    Programas
    Wait For Elements State    css=[aria-label="Programas vigentes do paciente"] >> text=${nome} >> nth=0    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Faltoso Pela Aba Do Paciente
    [Documentation]    Na aba Programas de Pacientes, marca o programa como faltoso com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nomePaciente}
    UI Pacientes - Abrir Paciente    ${nomePaciente}
    Clicar Aba    Programas
    Clicar Botao    Marcar faltoso: ${nome}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoSituacaoPrograma
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoSituacaoPrograma    Não veio às últimas consultas
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Marcado como faltoso
    ${depois}=    GET Inscritos Do Programa    ${programa}
    Should Be Equal As Integers    ${depois.json()}[faltosos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Busca Ativa Do Faltoso
    [Documentation]    Na lista de acompanhados: gerar a busca ativa do faltoso e confirmá-la com o resultado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${faltoso}=    Create Dictionary    situacao=FALTOSO    motivo=Não veio às consultas
    ${s}=    POST Situacao Acompanhamento    ${acompanhamento}    ${faltoso}
    Should Be Equal As Integers    ${s.status_code}    200
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Gerar busca ativa: ${nomePaciente}
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Busca ativa gerada
    Clicar Botao    Confirmar: Busca ativa ${nomePaciente}
    Select Options By    id=f-acao-resultado    value    ENCONTRADO_AGENDADO
    Click    css=app-drawer footer >> role=button[name="Confirmar"]
    Wait For Elements State    css=[aria-label="Ações programáticas"] .tag >> text=Realizada    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Prever Acao Do Programa
    [Documentation]    No detalhe do programa: prever a aferição de um acompanhado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Prever ação
    Select Options By    id=f-acao-tipo    value    AFERICAO
    Wait For Elements State    css=#f-acao-acompanhamento >> option[value="${acompanhamento}"]    attached
    Select Options By    id=f-acao-acompanhamento    value    ${acompanhamento}
    Click    css=app-drawer footer >> role=button[name="Prever ação"]
    Wait For Elements State    css=[aria-label="Ações programáticas"] >> text=Aferição    visible
    Wait For Elements State    css=[aria-label="Ações programáticas"] >> text=${nomePaciente}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Aba Acoes Nao Realizada
    [Documentation]    Na aba Ações programáticas: a ação da semana e o registro de não realizada, com o motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=VACINA    acompanhamentoId=${acompanhamento}    dataPrevista=${hoje}
    ${a}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${a.status_code}    201
    UI Programas - Abrir
    Clicar Aba    Ações programáticas
    Select Options By    css=[aria-label="Programa das ações"]    value    ${programa}
    Clicar Botao    Não realizada: Vacina ${nomePaciente}
    Fill Text    id=f-acao-motivo    Vacina em falta na unidade
    Click    css=app-drawer footer >> role=button[name="Salvar"]
    Wait For Elements State    css=[aria-label="Ações programáticas"] .tag >> text=Não realizada    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Cadastrar Campanha E Local
    [Documentation]    Aba Campanhas: cadastrar a campanha, incluir o local e publicar.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=20)).isoformat()    modules=datetime
    ${unidade}=    Seed A Unidade De Saude Uuid
    UI Programas - Abrir
    Clicar Aba    Campanhas
    Clicar Botao    Nova campanha
    Fill Text    id=f-campanha-nome    Campanha UI ${sufixo}
    Fill Text    id=f-campanha-inicio    ${hoje}
    Fill Text    id=f-campanha-fim    ${fim}
    Fill Text    id=f-campanha-idade-min    60
    Fill Text    id=f-campanha-meta-qtd    200
    Clicar Botao    Salvar campanha
    Wait For Elements State    css=app-drawer .tag >> text=Em andamento    visible
    Select Options By    id=f-local-unidade    value    ${unidade}
    Clicar Botao    Incluir local
    Wait For Elements State    css=[aria-label="Locais da campanha"] li    visible
    Clicar Botao    Publicar na consulta pública
    Wait For Elements State    css=app-drawer .tag >> text=Publicada    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Campanha Na Landing
    [Documentation]    A landing page mostra a campanha publicada, com o público e o local, sem login.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${payload}=    Create Dictionary    publicada=${True}
    ${r}=    POST Publicacao Campanha    ${campanha}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Ir Para Rota Do Frontend    /
    Scroll To Element    id=campanhas
    Wait For Elements State    css=#campanhas article[aria-label="${nome}"]    visible
    ${texto}=    Get Text    css=#campanhas article[aria-label="${nome}"]
    Should Contain    ${texto}    60 anos ou mais
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Acao Da Campanha
    [Documentation]    Na gaveta da campanha: prever a vacinação no local e confirmar com as pessoas atendidas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    UI Programas - Abrir
    Clicar Aba    Campanhas
    Clicar Botao    Abrir campanha: ${nome}
    Wait For Elements State    css=[aria-label="Locais da campanha"] li    visible
    Click    css=app-drawer >> role=button[name="Prever ação"]
    Click    css=app-drawer >> nth=1 >> css=footer >> role=button[name="Prever ação"]
    Wait For Elements State    css=[aria-label="Ações programáticas"] >> text=Vacina    visible
    ${detalhe}=    GET Campanha    ${campanha}
    ${nomeLocal}=    Set Variable    ${detalhe.json()}[locais][0][nome]
    Clicar Botao    Confirmar: Vacina ${nomeLocal}
    Fill Text    id=f-acao-quantidade    30
    Click    css=app-drawer >> nth=1 >> css=footer >> role=button[name="Confirmar"]
    Wait For Elements State    css=[aria-label="Andamento da meta"] >> text=30 de 100    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Indicadores Na Tela
    [Documentation]    No detalhe do HiperDia, os indicadores da unidade e a evolução de 12 meses (ADR-0141).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nomeUnidade}=    Seed HiperDia Na Unidade
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    HiperDia
    Select Options By    id=f-ind-unidade    value    ${unidade}
    Wait For Elements State    css=[aria-label="Indicadores do programa"] >> text=PA controlada    visible
    Wait For Elements State    css=[aria-label="Indicadores do programa"] >> text=Acompanhados em dia    visible
    Clicar Botao    Evolução: PA controlada
    Wait For Elements State    css=svg[aria-label="Gráfico: PA controlada"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Cadastrar Meta
    [Documentation]    Cadastra a meta da unidade pela tela e a vê na lista de metas (ADR-0141).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nomeUnidade}=    Seed HiperDia Na Unidade
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    HiperDia
    Wait For Elements State    css=[aria-label="Indicadores do programa"] >> text=Acompanhados em dia    visible
    Select Options By    id=f-meta-indicador    value    ACOMPANHADOS_EM_DIA
    Select Options By    id=f-meta-unidade    value    ${unidade}
    Fill Text    id=f-meta-alvo    70
    Clicar Botao    Cadastrar meta
    Wait For Elements State    css=[aria-label="Metas do programa"] >> text=${nomeUnidade}    visible
    ${metas}=    GET Metas Do Programa    ${programa}
    ${daUnidade}=    Evaluate    [m for m in $metas.json() if m.get("unidadeId") == $unidade]
    Length Should Be    ${daUnidade}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
