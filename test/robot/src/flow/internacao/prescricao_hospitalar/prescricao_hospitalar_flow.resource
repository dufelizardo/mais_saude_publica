*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/internacao/prescricao_hospitalar/prescricao_hospitalar_pom.resource
Resource    ../leito_internacao/leito_internacao_flow.resource
Resource    ../../lote/create_lote/create_lote_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Prescrição hospitalar (ADR-0187). O início é a hora cheia atual (em UTC), para os horários caírem perto de agora.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Hora Cheia Atual
    [Documentation]    The current full hour in ISO UTC, plus N hours.
    [Arguments]    ${horas}=0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${t}=    Evaluate    (datetime.datetime.now(datetime.timezone.utc).replace(minute=0, second=0, microsecond=0) + datetime.timedelta(hours=int($horas))).strftime('%Y-%m-%dT%H:%M:%SZ')    modules=datetime
    RETURN    ${t}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Prescricao
    [Documentation]    An admission with a medication (6/6 h), a soft diet and a care (4/4 h), from the current full hour; returns the prescription,
    ...    the admission, the unit, the medication and the doctor registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacao}    ${leito}    ${unidade}    ${setor}    ${paciente}    ${matricula}=    Seed Uma Internacao
    ${medicamento}    ${nome}=    Seed Um Medicamento
    ${inicio}=    Hora Cheia Atual
    ${remedio}=    Create Dictionary    tipo=MEDICAMENTO    medicamentoId=${medicamento}    dose=1 g    via=INTRAVENOSA    intervaloHoras=${6}
    ${dieta}=    Create Dictionary    tipo=DIETA    tipoDieta=BRANDA
    ${cuidado}=    Create Dictionary    tipo=CUIDADO    descricao=Sinais vitais    intervaloHoras=${4}
    ${itens}=    Create List    ${remedio}    ${dieta}    ${cuidado}
    ${payload}=    Create Dictionary    internacaoId=${internacao}    medicoMatricula=${matricula}    inicio=${inicio}    itens=${itens}
    ${r}=    POST Prescrever    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${internacao}    ${unidade}    ${medicamento}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prescricao - Prescrever - Status Code 201 400 E 409
    [Documentation]    The prescription schedules the times; without items gives 400; another one at the same start gives 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}    ${internacao}    ${unidade}    ${medicamento}    ${matricula}=    Seed Uma Prescricao
    ${d}=    GET Prescricao    ${p}
    Should Be Equal As Integers    ${d.status_code}    200
    Length Should Be    ${d.json()}[itens]    3
    Length Should Be    ${d.json()}[itens][0][horarios]    4
    Length Should Be    ${d.json()}[itens][1][horarios]    6
    Length Should Be    ${d.json()}[itens][2][horarios]    6
    ${inicio}=    Hora Cheia Atual
    ${vazia}=    Create List
    ${sem}=    Create Dictionary    internacaoId=${internacao}    medicoMatricula=${matricula}    inicio=${inicio}    itens=${vazia}
    ${r}=    POST Prescrever    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${cuidado}=    Create Dictionary    tipo=CUIDADO    descricao=Decúbito elevado
    ${itens}=    Create List    ${cuidado}
    ${outra}=    Create Dictionary    internacaoId=${internacao}    medicoMatricula=${matricula}    inicio=${inicio}    itens=${itens}
    ${r}=    POST Prescrever    ${outra}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prescricao - Checar Com Baixa Do Lote
    [Documentation]    Not done without a reason gives 400; done with a lot of the unit lowers the balance; checked again gives 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}    ${internacao}    ${unidade}    ${medicamento}    ${matricula}=    Seed Uma Prescricao
    ${payload}    ${numero}=    Build Lote Payload    ${medicamento}    ${unidade}
    ${l}=    POST Create Lote    ${payload}
    Should Be Equal As Integers    ${l.status_code}    201
    ${lote}=    Buscar Lote Uuid Por NumeroLote    ${numero}
    ${d}=    GET Prescricao    ${p}
    ${horario}=    Set Variable    ${d.json()}[itens][0][horarios][0][uuid]
    ${sem}=    Create Dictionary    profissionalMatricula=${matricula}    resultado=NAO_FEITO
    ${r}=    POST Acao Prescricao    horarios/${horario}/checagem    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${feito}=    Create Dictionary    profissionalMatricula=${matricula}    resultado=FEITO    loteId=${lote}    quantidade=${2}
    ${r}=    POST Acao Prescricao    horarios/${horario}/checagem    ${feito}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[itens][0][horarios][0][situacao]    FEITO
    Get Mais Saude Publica Session
    ${saldo}=    GET On Session    msp_session    ${BASE_URL}/lote/${lote}
    Should Be Equal As Integers    ${saldo.json()}[quantidade]    98
    ${r}=    POST Acao Prescricao    horarios/${horario}/checagem    ${feito}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prescricao - Suspender E Renovar
    [Documentation]    The suspended item cancels its pending times and is not copied by the renewal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}    ${internacao}    ${unidade}    ${medicamento}    ${matricula}=    Seed Uma Prescricao
    ${d}=    GET Prescricao    ${p}
    ${cuidado}=    Set Variable    ${d.json()}[itens][2][uuid]
    ${suspende}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Paciente estável.
    ${r}=    POST Acao Prescricao    itens/${cuidado}/suspensao    ${suspende}
    Should Be Equal As Integers    ${r.status_code}    200
    ${situacoes}=    Evaluate    {h['situacao'] for h in $r.json()['itens'][2]['horarios']}
    Should Be Equal    ${situacoes}    ${{ {'CANCELADO'} }}
    ${renova}=    Create Dictionary    medicoMatricula=${matricula}
    ${n}=    POST Acao Prescricao    ${p}/renovacao    ${renova}
    Should Be Equal As Integers    ${n.status_code}    201
    Length Should Be    ${n.json()}[itens]    2
    Should Be Equal    ${n.json()}[renovadaDe]    ${p}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prescricao - Reaprazar E Posto
    [Documentation]    Rescheduling outside the validity gives 422; inside, with a reason, moves the time; the station shows the times.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}    ${internacao}    ${unidade}    ${medicamento}    ${matricula}=    Seed Uma Prescricao
    ${d}=    GET Prescricao    ${p}
    ${horario}=    Set Variable    ${d.json()}[itens][0][horarios][1][uuid]
    ${fora}=    Hora Cheia Atual    30
    ${p1}=    Create Dictionary    profissionalMatricula=${matricula}    previstoPara=${fora}    motivo=Exame.
    ${r}=    POST Acao Prescricao    horarios/${horario}/reaprazamento    ${p1}
    Should Be Equal As Integers    ${r.status_code}    422
    ${dentro}=    Hora Cheia Atual    7
    ${p2}=    Create Dictionary    profissionalMatricula=${matricula}    previstoPara=${dentro}    motivo=Paciente em exame.
    ${r}=    POST Acao Prescricao    horarios/${horario}/reaprazamento    ${p2}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[itens][0][horarios][1][motivoReaprazamento]    Paciente em exame.
    ${posto}=    GET Posto De Enfermagem    ${unidade}
    Should Be Equal As Integers    ${posto.status_code}    200
    ${ids}=    Evaluate    [h['internacaoId'] for h in $posto.json()]
    Should Contain    ${ids}    ${internacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prescricao - Dieta E Retificacao
    [Documentation]    The diet needs the acceptance (400 without it); the check is corrected by a rectification with a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}    ${internacao}    ${unidade}    ${medicamento}    ${matricula}=    Seed Uma Prescricao
    ${d}=    GET Prescricao    ${p}
    ${cuidado}=    Set Variable    ${d.json()}[itens][2][horarios][0][uuid]
    ${refeicoes}=    Set Variable    ${d.json()}[itens][1][horarios]
    ${dieta}=    Set Variable    ${refeicoes}[0][uuid]
    ${sem}=    Create Dictionary    profissionalMatricula=${matricula}    resultado=FEITO
    ${r}=    POST Acao Prescricao    horarios/${dieta}/checagem    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${com}=    Create Dictionary    profissionalMatricula=${matricula}    resultado=FEITO    aceitacao=PARCIAL
    ${r}=    POST Acao Prescricao    horarios/${dieta}/checagem    ${com}
    Should Be Equal As Integers    ${r.status_code}    200
    ${feito}=    Create Dictionary    profissionalMatricula=${matricula}    resultado=FEITO
    ${r}=    POST Acao Prescricao    horarios/${cuidado}/checagem    ${feito}
    Should Be Equal As Integers    ${r.status_code}    200
    ${checagem}=    Set Variable    ${r.json()}[itens][2][horarios][0][checagem][uuid]
    ${ret}=    Create Dictionary    profissionalMatricula=${matricula}    resultado=NAO_FEITO    observacao=Paciente no centro cirúrgico.
    ...    motivoRetificacao=Checado por engano.
    ${r}=    POST Acao Prescricao    checagens/${checagem}/retificacao    ${ret}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[itens][2][horarios][0][situacao]    NAO_FEITO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prescricao - By Cenario
    [Documentation]    Executes the Prescricao scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'PRESCREVER'
        Prescricao - Prescrever - Status Code 201 400 E 409
    ELSE IF    '${caso}' == 'CHECAR_LOTE'
        Prescricao - Checar Com Baixa Do Lote
    ELSE IF    '${caso}' == 'SUSPENDER_RENOVAR'
        Prescricao - Suspender E Renovar
    ELSE IF    '${caso}' == 'REAPRAZAR_POSTO'
        Prescricao - Reaprazar E Posto
    ELSE IF    '${caso}' == 'DIETA_RETIFICACAO'
        Prescricao - Dieta E Retificacao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Prescricao.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
