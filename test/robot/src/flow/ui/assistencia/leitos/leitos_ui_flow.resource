*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/leitos/leitos_ui_pom.resource
Resource    ../../../internacao/leito_internacao/leito_internacao_flow.resource
Resource    ../../../atendimento/create_atendimento/create_atendimento_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os dados de apoio (setor, leito, paciente, internação) são criados pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Leitos - Seed Leito Com Nome
    [Documentation]    Cria um leito com identificação conhecida no setor e devolve uuid e identificação.
    [Arguments]    ${unidadeId}    ${setorId}    ${sexo}=MISTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${identificacao}=    Set Variable    UI Leito ${sufixo}
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    setorId=${setorId}    identificacao=${identificacao}    tipo=CLINICO    sexo=${sexo}
    ${response}=    POST Create Leito    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[uuid]    ${identificacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Situacao Do Leito Deve Ser
    [Documentation]    Confere pela API a situação do leito na unidade.
    [Arguments]    ${unidadeId}    ${leitoId}    ${esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${mapa}=    GET Mapa Leitos    ${params}
    ${situacoes}=    Evaluate    {l["uuid"]: l["situacao"] for l in $mapa.json()}
    Should Be Equal    ${situacoes}[${leitoId}]    ${esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Internar Pelo Mapa
    [Documentation]    Interna um paciente clicando no leito livre do mapa.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${leitoId}    ${identificacao}=    UI Leitos - Seed Leito Com Nome    ${unidadeId}    ${setorId}
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Internar no leito ${identificacao}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Internar no leito ${identificacao}
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Escolher Opcao Da Gaveta    pacienteId    ${pacienteId}
    Preencher Campo Da Gaveta    cid    J18.9
    Preencher Campo Da Gaveta    medicoMatricula    ${matricula}
    Preencher Campo Da Gaveta    motivo    Pneumonia com hipoxemia, sem resposta ao tratamento oral.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente internado
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    OCUPADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Gaveta Recusa Internacao Vazia
    [Documentation]    Envia a internação vazia e vê as mensagens do paciente e do leito.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Internar paciente
    Preencher Campo Da Gaveta    medicoMatricula    ${EMPTY}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    pacienteId
    Should Contain    ${erro}    Selecione o paciente
    ${erroLeito}=    Ler Erro Do Campo Da Gaveta    leitoId
    Should Contain    ${erroLeito}    leito livre
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Alta E Liberacao
    [Documentation]    Abre a internação pelo mapa, dá alta e libera o leito depois da higienização.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${lida}=    GET Internacao By Id    ${internacaoId}
    ${identificacao}=    Set Variable    ${lida.json()}[leitoIdentificacao]
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Abrir a internação do leito ${identificacao}
    Esperar Texto Aparecer    Motivo da internação
    Clicar Botao    Dar alta
    Preencher Campo Da Gaveta    sumario    Pneumonia tratada com antibiótico venoso; alta com antibiótico oral.
    Preencher Campo Da Gaveta    matriculaAlta    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Alta registrada
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    HIGIENIZACAO
    Clicar Botao    Liberar o leito ${identificacao}
    Esperar Texto Aparecer    Leito liberado
    Wait Until Keyword Succeeds    10s    500ms    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Trocar De Leito
    [Documentation]    Troca o paciente para outro leito livre do setor.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${destino}    ${identDestino}=    UI Leitos - Seed Leito Com Nome    ${unidadeId}    ${setorId}    FEMININO
    ${lida}=    GET Internacao By Id    ${internacaoId}
    ${identificacao}=    Set Variable    ${lida.json()}[leitoIdentificacao]
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Abrir a internação do leito ${identificacao}
    Esperar Texto Aparecer    Motivo da internação
    Clicar Botao    Trocar de leito
    Escolher Opcao Da Gaveta    leitoDestino    ${destino}
    Preencher Campo Da Gaveta    motivoTroca    Isolamento de contato
    Preencher Campo Da Gaveta    matriculaTroca    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito trocado
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${destino}    OCUPADO
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    HIGIENIZACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Bloquear E Desbloquear
    [Documentation]    Bloqueia um leito livre com motivo e desbloqueia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${leitoId}    ${identificacao}=    UI Leitos - Seed Leito Com Nome    ${unidadeId}    ${setorId}
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Bloquear o leito ${identificacao}
    Preencher Campo Da Gaveta    motivoBloqueio    Manutenção da cama
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito bloqueado
    Clicar Botao    Desbloquear o leito ${identificacao}
    Esperar Texto Aparecer    Leito desbloqueado
    Wait Until Keyword Succeeds    10s    500ms    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Cadastrar Leito Pela Gaveta
    [Documentation]    Cadastra um leito num setor assistencial e vê o aviso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    UI Leitos - Abrir Aba    leitos
    Clicar Botao    + Leito
    Escolher Opcao Da Gaveta    unidadeLeito    ${unidadeId}
    Escolher Opcao Da Gaveta    setorLeito    ${setorId}
    Preencher Campo Da Gaveta    identificacao    UI Novo ${sufixo}
    Escolher Opcao Da Gaveta    tipoLeito    UTI_ADULTO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito cadastrado
    Esperar Texto Aparecer    UI Novo ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Link Do Atendimento Abre A Internacao
    [Documentation]    O link do "Internar" no atendimento abre a internação com o paciente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    UI Leitos - Abrir Internar Pelo Link    ${pacienteId}    ${unidadeId}    ${atendimentoId}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Internar paciente
    Wait Until Keyword Succeeds    10s    500ms    UI Leitos - Campo Deve Valer    pacienteId    ${pacienteId}
    Esperar Texto Aparecer    ligada ao atendimento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Campo Deve Valer
    [Documentation]    Confere o valor de um campo da gaveta.
    [Arguments]    ${campo}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    UI Leitos - Ler Valor Do Campo    ${campo}
    Should Be Equal    ${valor}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Internacao No Prontuario
    [Documentation]    A internação aparece no prontuário do paciente, com o leito e o motivo (ADR-0100).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${lida}=    GET Internacao By Id    ${internacaoId}
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${pacienteId}
    Esperar Texto Aparecer    Internações
    Wait For Elements State    css=app-internacoes-paciente >> text=${lida.json()}[leitoIdentificacao]    visible    10s
    Wait For Elements State    css=app-internacoes-paciente >> text=Pneumonia com hipoxemia.    visible    10s
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
