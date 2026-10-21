*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/programas/acao/acao_programatica_pom.resource
Resource    ../acompanhamento/acompanhamento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Acao - Individual - Status Code 201
    [Documentation]    Plans a scheduled consultation for a follow-up; past date is overdue.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=CONSULTA_PROGRAMADA    acompanhamentoId=${acompanhamento}    dataPrevista=${ontem}
    ${r}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${ACAO_PROGRAMATICA_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    PENDENTE
    Should Be True    ${r.json()}[vencida]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Consulta Sem Acompanhamento - Status Code 400
    [Documentation]    A scheduled consultation needs the follow-up.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=CONSULTA_PROGRAMADA    unidadeId=${unidade}    dataPrevista=${ontem}
    ${r}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Coletiva - Status Code 201
    [Documentation]    A group with a participant followed in the program.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${participantes}=    Create List    ${paciente}
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=GRUPO    unidadeId=${unidade}    local=Salão    participantesIds=${participantes}    dataPrevista=${ontem}
    ${r}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be True    ${r.json()}[coletiva]
    Length Should Be    ${r.json()}[participantes]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Participante Fora Do Programa - Status Code 422
    [Documentation]    Participants must be followed in the program.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${outro}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${participantes}=    Create List    ${outro}
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=GRUPO    unidadeId=${unidade}    participantesIds=${participantes}    dataPrevista=${ontem}
    ${r}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Realizacao - Status Code 200
    [Documentation]    Confirms the action; confirming again is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=AFERICAO    acompanhamentoId=${acompanhamento}    dataPrevista=${ontem}
    ${a}=    POST Acao Programatica    ${payload}
    ${obs}=    Create Dictionary    observacao=PA 130x85
    ${r}=    POST Realizacao Acao    ${a.json()}[uuid]    ${obs}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    REALIZADA
    ${r2}=    POST Realizacao Acao    ${a.json()}[uuid]    ${obs}
    Should Be Equal As Integers    ${r2.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Nao Realizada - Status Code 200
    [Documentation]    Not done with the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=EXAME    acompanhamentoId=${acompanhamento}    dataPrevista=${ontem}
    ${a}=    POST Acao Programatica    ${payload}
    ${situacao}=    Create Dictionary    situacao=NAO_REALIZADA    motivo=Faltou
    ${r}=    POST Situacao Acao    ${a.json()}[uuid]    ${situacao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[motivo]    Faltou
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Busca Ativa - Status Code 201
    [Documentation]    Active search of the follow-up; a second pending one is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${vazio}=    Create Dictionary
    ${r}=    POST Busca Ativa    ${acompanhamento}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[tipo]    BUSCA_ATIVA
    ${r2}=    POST Busca Ativa    ${acompanhamento}    ${vazio}
    Should Be Equal As Integers    ${r2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Busca Sem Resultado - Status Code 400
    [Documentation]    Confirming the active search needs the result; moved suggests closing.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${vazio}=    Create Dictionary
    ${b}=    POST Busca Ativa    ${acompanhamento}    ${vazio}
    ${r}=    POST Realizacao Acao    ${b.json()}[uuid]    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    400
    ${resultado}=    Create Dictionary    resultadoBusca=MUDOU_DE_ENDERECO
    ${r2}=    POST Realizacao Acao    ${b.json()}[uuid]    ${resultado}
    Should Be Equal As Integers    ${r2.status_code}    200
    Should Contain    ${r2.json()}[sugestao]    encerrar o acompanhamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acoes - Lista - Status Code 200
    [Documentation]    The list brings the overdue action and the indicators.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=VACINA    acompanhamentoId=${acompanhamento}    dataPrevista=${ontem}
    POST Acao Programatica    ${payload}
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Acoes Programaticas    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${ACOES_PROGRAMATICAS_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[vencidas]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acoes - Do Paciente - Status Code 200
    [Documentation]    The patient actions.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${ontem}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=ORIENTACAO    acompanhamentoId=${acompanhamento}    dataPrevista=${ontem}
    POST Acao Programatica    ${payload}
    ${r}=    GET Acoes Do Paciente    ${paciente}
    Should Be Equal As Integers    ${r.status_code}    200
    Length Should Be    ${r.json()}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao - Inexistente - Status Code 404
    [Documentation]    Confirming an action that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${vazio}=    Create Dictionary
    ${r}=    POST Realizacao Acao    ${uuid}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acao Programatica - By HTTP Status Code
    [Documentation]    Executes the P4 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-individual'    Acao - Individual - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-individual'    Acao - Consulta Sem Acompanhamento - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-coletiva'    Acao - Coletiva - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-participante'    Acao - Participante Fora Do Programa - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-realizar'    Acao - Realizacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-nao-realizada'    Acao - Nao Realizada - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-busca-ativa'    Acao - Busca Ativa - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-busca-resultado'    Acao - Busca Sem Resultado - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-lista'    Acoes - Lista - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-paciente'    Acoes - Do Paciente - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404-acao'    Acao - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Acao Programatica test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
