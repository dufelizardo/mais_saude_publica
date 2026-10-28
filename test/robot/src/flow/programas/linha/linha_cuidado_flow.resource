*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/programas/linha/linha_cuidado_pom.resource
Resource    ../../imunizacao/vacinacao_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A paciente na linha da gestante aberta há 100 dias, e uma linha com etapa manual e de vacina (ADR-0152).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Da Linha
    [Documentation]    Returns the uuid of a care pathway by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Linhas De Cuidado
    ${ids}=    Evaluate    [l["uuid"] for l in $r.json() if l["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Etapa Do Percurso
    [Documentation]    Returns the step of the pathway by order.
    [Arguments]    ${percurso}    ${ordem}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${e}=    Evaluate    [x for x in $percurso["etapas"] if x["ordem"] == int($ordem)]
    RETURN    ${e}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Linha Manual
    [Documentation]    Creates a pathway with a manual step and a BCG step; returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${bcg}=    Id Do Imunobiologico    BCG
    ${e1}=    Create Dictionary    nome=Visita domiciliar    tipo=MANUAL    prazoDias=${30}
    ${e2}=    Create Dictionary    nome=BCG    tipo=VACINA    imunobiologicoId=${bcg}    prazoDias=${30}
    ${etapas}=    Create List    ${e1}    ${e2}
    ${payload}=    Create Dictionary    codigo=ROBOT_${sufixo}    nome=Robot Linha ${sufixo}    etapas=${etapas}
    ${r}=    POST Linha De Cuidado    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Abrir Percurso
    [Documentation]    Opens the pathway for the seeded patient, started some days ago; returns the response.
    [Arguments]    ${dados}    ${linha}    ${dias}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${inicio}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=int(${dias}))).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    pacienteId=${dados}[paciente]    linhaId=${linha}    inicio=${inicio}    unidadeId=${dados}[unidade]
    ${r}=    POST Percurso Linha    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso Da Gestante
    [Documentation]    Seeds a patient in the pregnancy pathway started 100 days ago; returns the data and the pathway.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${gestante}=    Id Da Linha    GESTANTE
    ${r}=    Abrir Percurso    ${dados}    ${gestante}    100
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${dados}    ${r.json()}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Linha De Cuidado - Catalogo - Status Code 200
    [Documentation]    The initial load brings the pregnancy pathway with six steps.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Linhas De Cuidado
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Array Schema    ${r.json()}    ${LINHA_CUIDADO_SCHEMA}
    ${g}=    Evaluate    [l for l in $r.json() if l["codigo"] == "GESTANTE"][0]
    Length Should Be    ${g}[etapas]    6
    Should Be Equal    ${g}[programaNome]    Pré-natal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Linha De Cuidado - Cadastro - Status Code 201
    [Documentation]    A pathway with a manual and a vaccine step.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${linha}=    Linha Manual
    Should Not Be Empty    ${linha}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Linha De Cuidado - Codigo Repetido - Status Code 409
    [Documentation]    The code is unique.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${e1}=    Create Dictionary    nome=X    tipo=MANUAL
    ${etapas}=    Create List    ${e1}
    ${payload}=    Create Dictionary    codigo=GESTANTE    nome=Outra    etapas=${etapas}
    ${r}=    POST Linha De Cuidado    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Linha De Cuidado - Exame Sem Exame - Status Code 400
    [Documentation]    The exam step needs the exam.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${e1}=    Create Dictionary    nome=Exame    tipo=EXAME
    ${etapas}=    Create List    ${e1}
    ${payload}=    Create Dictionary    codigo=ROBOT_${sufixo}    nome=Sem exame    etapas=${etapas}
    ${r}=    POST Linha De Cuidado    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Abertura - Status Code 201
    [Documentation]    The pregnancy pathway 100 days in, without the first visit, is late.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${p}=    Percurso Da Gestante
    Validate Object Schema    ${p}    ${PERCURSO_LINHA_SCHEMA}
    Validate Array Schema    ${p}[etapas]    ${ETAPA_PERCURSO_SCHEMA}
    Should Be True    ${p}[atrasado]
    ${e1}=    Etapa Do Percurso    ${p}    1
    Should Be Equal    ${e1}[situacao]    ATRASADA
    ${e6}=    Etapa Do Percurso    ${p}    6
    Should Be Equal    ${e6}[situacao]    AGUARDANDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Duplicado - Status Code 409
    [Documentation]    One pathway in progress per patient and line.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${p}=    Percurso Da Gestante
    ${r}=    Abrir Percurso    ${dados}    ${p}[linhaId]    50
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Etapa Manual - Status Code 200
    [Documentation]    The manual step is registered by hand.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${linha}=    Linha Manual
    ${r}=    Abrir Percurso    ${dados}    ${linha}    10
    ${e1}=    Etapa Do Percurso    ${r.json()}    1
    ${payload}=    Create Dictionary    etapaId=${e1}[etapaId]    tipo=CUMPRIDA    observacao=Visita feita
    ${p}=    POST Etapa Percurso    ${r.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${p.status_code}    200
    ${depois}=    Etapa Do Percurso    ${p.json()}    1
    Should Be Equal    ${depois}[situacao]    CUMPRIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Etapa Do Dominio A Mao - Status Code 422
    [Documentation]    A domain step is not registered by hand.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${p}=    Percurso Da Gestante
    ${e1}=    Etapa Do Percurso    ${p}    1
    ${payload}=    Create Dictionary    etapaId=${e1}[etapaId]    tipo=CUMPRIDA
    ${r}=    POST Etapa Percurso    ${p}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Dispensa - Status Code 200
    [Documentation]    Waiving needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${p}=    Percurso Da Gestante
    ${e3}=    Etapa Do Percurso    ${p}    3
    ${sem}=    Create Dictionary    etapaId=${e3}[etapaId]    tipo=DISPENSADA
    ${r}=    POST Etapa Percurso    ${p}[uuid]    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${com}=    Create Dictionary    etapaId=${e3}[etapaId]    tipo=DISPENSADA    observacao=Vacinada em outro município
    ${ok}=    POST Etapa Percurso    ${p}[uuid]    ${com}
    Should Be Equal As Integers    ${ok.status_code}    200
    ${depois}=    Etapa Do Percurso    ${ok.json()}    3
    Should Be Equal    ${depois}[situacao]    DISPENSADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Vacina Cumpre - Status Code 200
    [Documentation]    The BCG dose after the start fulfills the vaccine step.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${linha}=    Linha Manual
    ${r}=    Abrir Percurso    ${dados}    ${linha}    10
    ${bcg}=    Id Do Imunobiologico    BCG
    ${quando}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=5)).isoformat() + 'T09:00:00'    modules=datetime
    ${dose}=    Create Dictionary    pacienteId=${dados}[paciente]    imunobiologicoId=${bcg}    dose=UNICA    unidadeId=${dados}[unidade]    origem=TRANSCRITA    aplicadaEm=${quando}
    ${d}=    POST Registro Vacinacao    ${dose}
    Should Be Equal As Integers    ${d.status_code}    201
    ${p}=    GET Percurso Linha    ${r.json()}[uuid]
    ${e2}=    Etapa Do Percurso    ${p.json()}    2
    Should Be Equal    ${e2}[situacao]    CUMPRIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Concluir Com Pendencia - Status Code 422
    [Documentation]    Concluding needs the mandatory steps done; interrupting needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${p}=    Percurso Da Gestante
    ${concluir}=    Create Dictionary    situacao=CONCLUIDO
    ${r}=    POST Encerramento Percurso    ${p}[uuid]    ${concluir}
    Should Be Equal As Integers    ${r.status_code}    422
    ${interromper}=    Create Dictionary    situacao=INTERROMPIDO    motivo=Mudou de município
    ${ok}=    POST Encerramento Percurso    ${p}[uuid]    ${interromper}
    Should Be Equal As Integers    ${ok.status_code}    200
    Should Be Equal    ${ok.json()}[situacao]    INTERROMPIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Percurso - Painel - Status Code 200
    [Documentation]    The panel of the unit lists the late pathway.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${p}=    Percurso Da Gestante
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]    atrasados=true
    ${r}=    GET Painel Linhas De Cuidado    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PAINEL_LINHAS_SCHEMA}
    Should Be Equal    ${r.json()}[percursos][0][uuid]    ${p}[uuid]
    ${l}=    GET Percursos Do Paciente    ${dados}[paciente]
    Should Be Equal    ${l.json()}[0][uuid]    ${p}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Linha De Cuidado - By HTTP Status Code
    [Documentation]    Executes the P5 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Linha De Cuidado - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-linha'    Linha De Cuidado - Cadastro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-codigo'    Linha De Cuidado - Codigo Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400-exame'    Linha De Cuidado - Exame Sem Exame - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-percurso'    Percurso - Abertura - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-percurso'    Percurso - Duplicado - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '200-manual'    Percurso - Etapa Manual - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-dominio'    Percurso - Etapa Do Dominio A Mao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-dispensa'    Percurso - Dispensa - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-vacina'    Percurso - Vacina Cumpre - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-concluir'    Percurso - Concluir Com Pendencia - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-painel'    Percurso - Painel - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Linha De Cuidado test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
