*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/regulacao/regulacao_ui_pom.resource
Resource    ../../../solicitacao_regulacao/get_solicitacao_regulacao_by_id/get_solicitacao_regulacao_by_id_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os dados de apoio (paciente, unidade, profissional, procedimento, solicitação) são criados pela API; a ação
    sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Regulacao - Nova Solicitacao Pela Gaveta
    [Documentation]    Encaminha um paciente pela gaveta e vê o aviso e a linha na aba Solicitações.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${procedimentoId}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    UI Regulacao - Abrir Aba    solicitacoes
    Clicar Botao    Nova solicitação
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Nova solicitação de regulação
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Escolher Opcao Da Gaveta    pacienteId    ${pacienteId}
    Escolher Opcao Da Gaveta    procedimentoId    ${procedimentoId}
    Escolher Opcao Da Gaveta    unidadeSolicitanteId    ${unidadeId}
    Preencher Campo Da Gaveta    cid    I20.9
    Preencher Campo Da Gaveta    profissionalMatricula    ${matricula}
    Preencher Campo Da Gaveta    justificativa    Dor precordial aos esforços há três meses, ECG alterado.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Solicitação registrada
    Esperar Texto Aparecer    ${nomePaciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Autorizar Pela Fila
    [Documentation]    Analisa uma solicitação da fila e autoriza com a vaga.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao    VERMELHO
    ${regulador}=    Seed A Profissional Matricula
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    ${nomePaciente}=    Set Variable    ${lida.json()}[pacienteNome]
    UI Regulacao - Abrir Aba    fila
    Clicar Botao    Analisar solicitação de ${nomePaciente}
    Esperar Texto Aparecer    Decisão da regulação
    Escolher Opcao Da Gaveta    unidadeExecutanteId    ${unidadeId}
    ${vaga}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=10)).strftime('%Y-%m-%dT09:30')    modules=datetime
    Preencher Campo Da Gaveta    dataHoraPrevista    ${vaga}
    Preencher Campo Da Gaveta    matriculaRegulador    ${regulador}
    Clicar Botao    Registrar decisão
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Solicitação autorizada
    ${depois}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${depois.json()}[status]    AUTORIZADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Devolver E Complementar
    [Documentation]    O regulador devolve; o solicitante complementa na aba Solicitações e a solicitação volta à fila.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${regulador}=    Seed A Profissional Matricula
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    ${nomePaciente}=    Set Variable    ${lida.json()}[pacienteNome]
    UI Regulacao - Abrir Aba    fila
    Clicar Botao    Analisar solicitação de ${nomePaciente}
    Esperar Texto Aparecer    Decisão da regulação
    UI Regulacao - Escolher Decisao    Devolver para complementar
    Preencher Campo Da Gaveta    motivoDecisao    Anexar o ECG
    Preencher Campo Da Gaveta    matriculaRegulador    ${regulador}
    Clicar Botao    Registrar decisão
    Esperar Texto Aparecer    Solicitação devolvida
    UI Regulacao - Abrir Aba    solicitacoes
    Clicar Botao    Complementar solicitação de ${nomePaciente}
    Esperar Texto Aparecer    Anexar o ECG
    Preencher Campo Da Gaveta    complemento    ECG de 10/09 com infradesnivelamento de ST.
    Preencher Campo Da Gaveta    matriculaComplemento    ${matricula}
    Clicar Botao    Devolver à fila
    Esperar Texto Aparecer    Solicitação complementada
    ${depois}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    Should Be Equal    ${depois.json()}[status]    SOLICITADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Gaveta Recusa Cid Invalido
    [Documentation]    Envia a nova solicitação com CID inválido e vê a mensagem do campo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Regulacao - Abrir Aba    solicitacoes
    Clicar Botao    Nova solicitação
    Preencher Campo Da Gaveta    cid    XYZ
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    cid
    Should Contain    ${erro}    CID-10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Cadastrar Procedimento Pela Gaveta
    [Documentation]    Cadastra um procedimento no catálogo e vê o aviso e a linha.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot UI Procedimento ${sufixo}
    UI Regulacao - Abrir Aba    procedimentos
    Clicar Botao    + Procedimento
    Preencher Campo Da Gaveta    nome    ${nome}
    Escolher Opcao Da Gaveta    tipo    EXAME
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Procedimento cadastrado
    Esperar Texto Aparecer    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Regulacao - Encaminhar Abre A Gaveta Preenchida
    [Documentation]    O link do "Encaminhar" abre a nova solicitação com o paciente e a unidade escolhidos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    UI Regulacao - Abrir Nova Solicitacao Pelo Link    ${pacienteId}    ${unidadeId}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Nova solicitação de regulação
    Wait Until Keyword Succeeds    10s    500ms    Campo Da Gaveta Deve Valer    pacienteId    ${pacienteId}
    ${unidade}=    UI Regulacao - Ler Valor Do Campo    unidadeSolicitanteId
    Should Be Equal    ${unidade}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campo Da Gaveta Deve Valer
    [Documentation]    Confere o valor de um campo da gaveta.
    [Arguments]    ${campo}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    UI Regulacao - Ler Valor Do Campo    ${campo}
    Should Be Equal    ${valor}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
