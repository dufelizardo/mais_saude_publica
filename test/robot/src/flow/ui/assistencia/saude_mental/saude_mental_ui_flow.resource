*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/saude_mental/saude_mental_ui_pom.resource
Resource    ../../../saude_mental/saude_mental_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base (unidade que oferece o programa, equipe, membro e paciente) é criada pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Mental - Titulo Deve Ser
    [Documentation]    Confere o paciente no detalhe do caso.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${titulo}=    UI Saude Mental - Ler Titulo Do Caso
    Should Be Equal    ${titulo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Acolher Abrindo O Caso
    [Documentation]    Sem paciente, a mensagem; com os dados e a referência, o caso abre no programa.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}=    Seed Base Saude Mental
    UI Saude Mental - Abrir
    Clicar Botao    Novo acolhimento
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    buscaPaciente
    Should Contain    ${erro}    paciente
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Click    css=app-drawer [role="listbox"] >> role=option[name="${nomePaciente}"]
    Escolher Opcao Da Gaveta    unidadeAcolhimento    ${unidade}
    Preencher Campo Da Gaveta    motivoProcura    Tristeza persistente e insônia há três meses.
    Escolher Opcao Da Gaveta    riscoInicial    MODERADO
    Escolher Opcao Da Gaveta    desfecho    ACOMPANHAMENTO_NA_UNIDADE
    UI Saude Mental - Esperar Opcao    equipeReferencia    ${equipe}
    Escolher Opcao Da Gaveta    equipeReferencia    ${equipe}
    UI Saude Mental - Esperar Opcao    tecnicoReferencia    ${matricula}
    Escolher Opcao Da Gaveta    tecnicoReferencia    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Acolhimento registrado
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${pacientes}=    Evaluate    [a["pacienteId"] for a in $inscritos.json()["lista"]]
    Should Contain    ${pacientes}    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Nova Avaliacao
    [Documentation]    Sem contexto, a mensagem; com os dados, a avaliação aparece na aba.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Clicar Botao    Nova avaliação
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    contextoHistoria
    Should Contain    ${erro}    contexto
    Preencher Campo Da Gaveta    contextoHistoria    Luto recente, afastada do trabalho.
    Preencher Campo Da Gaveta    necessidades    Acompanhamento semanal e rede de apoio.
    Preencher Campo Da Gaveta    cid    F32.1
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Avaliação registrada
    Wait For Elements State    css=[aria-label="Avaliações do caso"] >> text=F32.1    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Trocar Referencia
    [Documentation]    Troca o técnico de referência; a anterior fica no histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${outro}=    Seed Um Profissional Lotado    ${unidade}
    ${membro}=    Create Dictionary    profissionalMatricula=${outro}    funcao=ASSISTENTE_SOCIAL
    ${r}=    POST Membro Equipe    ${equipe}    ${membro}
    Should Be Equal As Integers    ${r.status_code}    200
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Clicar Botao    Trocar referência
    UI Saude Mental - Esperar Opcao    equipeTroca    ${equipe}
    Escolher Opcao Da Gaveta    equipeTroca    ${equipe}
    UI Saude Mental - Esperar Opcao    tecnicoTroca    ${outro}
    Escolher Opcao Da Gaveta    tecnicoTroca    ${outro}
    Preencher Campo Da Gaveta    motivoTroca    Vínculo maior com a assistente social.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Referência trocada
    ${depois}=    GET Caso Saude Mental    ${caso}
    Should Be Equal    ${depois.json()}[tecnicoMatricula]    ${outro}
    Length Should Be    ${depois.json()}[referencias]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Encerrar Caso
    [Documentation]    Encerra o caso com alta; o caso fica concluído.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Clicar Botao    Encerrar
    Escolher Opcao Da Gaveta    motivoSaidaCaso    ALTA
    Preencher Campo Da Gaveta    observacaoSaidaCaso    Objetivos do cuidado alcançados.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Caso encerrado
    ${depois}=    GET Caso Saude Mental    ${caso}
    Should Be Equal    ${depois.json()}[situacao]    CONCLUIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
