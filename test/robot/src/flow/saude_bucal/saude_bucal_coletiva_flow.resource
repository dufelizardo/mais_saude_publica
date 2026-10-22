*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_bucal/saude_bucal_coletiva_pom.resource
Resource    ../programas/acao/acao_programatica_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Acao Bucal Coletiva
    [Documentation]    Offers the catalog program in a fresh UBS, enrolls a patient and plans a group with him; returns the action, the patient and the unit.
    [Arguments]    ${codigo}=SAUDE_NA_ESCOLA    ${comLista}=${True}    ${local}=Escola Municipal
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Programas
    ${ids}=    Evaluate    [p["uuid"] for p in $lista.json() if p["codigo"] == $codigo]
    ${programa}=    Set Variable    ${ids}[0]
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${part}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    POST Participacao Programa    ${programa}    ${part}
    Should Be Equal As Integers    ${r.status_code}    200
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    IF    ${comLista}
        ${insc}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}
        ${i}=    POST Inscrever Paciente No Programa    ${programa}    ${insc}
        Should Be Equal As Integers    ${i.status_code}    201
    END
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${participantes}=    Create List
    IF    ${comLista}    Append To List    ${participantes}    ${paciente}
    ${payload}=    Create Dictionary    programaId=${programa}    tipo=GRUPO    unidadeId=${unidade}    local=${local}    participantesIds=${participantes}    dataPrevista=${hoje}
    ${a}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${a.status_code}    201
    RETURN    ${a.json()}[uuid]    ${paciente}    ${unidade}    ${nomePaciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Detalhe Da Escovacao
    [Documentation]    The oral health detail of a supervised brushing at school.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atividades}=    Create List    ESCOVACAO_SUPERVISIONADA    APLICACAO_TOPICA_FLUOR
    ${payload}=    Create Dictionary    atividades=${atividades}    localTipo=ESCOLA    instituicao=Escola Municipal Rui Barbosa    publicoAlvo=1º ao 5º ano    kitsDistribuidos=${30}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal Coletiva - Detalhe - Status Code 200
    [Documentation]    Fills the detail of a PSE collective action.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}    ${paciente}    ${unidade}    ${nome}=    Seed Acao Bucal Coletiva
    ${payload}=    Detalhe Da Escovacao
    ${r}=    PUT Saude Bucal Da Acao    ${acao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${ACAO_SAUDE_BUCAL_SCHEMA}
    Should Be True    ${r.json()}[preenchido]
    Should Be Equal As Integers    ${r.json()}[kitsDistribuidos]    30
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal Coletiva - Programa Sem Odontologia - Status Code 422
    [Documentation]    The detail is only for oral health and PSE programs.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}    ${paciente}    ${unidade}    ${nome}=    Seed Acao Bucal Coletiva    HIPERDIA    ${False}
    ${payload}=    Detalhe Da Escovacao
    ${r}=    PUT Saude Bucal Da Acao    ${acao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal Coletiva - Sem Atividade - Status Code 400
    [Documentation]    The detail needs at least one activity.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}    ${paciente}    ${unidade}    ${nome}=    Seed Acao Bucal Coletiva
    ${vazia}=    Create List
    ${payload}=    Create Dictionary    atividades=${vazia}    localTipo=ESCOLA
    ${r}=    PUT Saude Bucal Da Acao    ${acao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal Coletiva - Avaliacao E A Convocar - Status Code 200
    [Documentation]    The assessment with urgency puts the participant in the list to be called.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}    ${paciente}    ${unidade}    ${nome}=    Seed Acao Bucal Coletiva    SAUDE_BUCAL
    ${vazio}=    Create Dictionary
    POST Realizacao Acao    ${acao}    ${vazio}
    ${resultado}=    Create Dictionary    pacienteId=${paciente}    risco=ALTO    necessidadeTratamento=URGENCIA
    ${resultados}=    Create List    ${resultado}
    ${payload}=    Create Dictionary    resultados=${resultados}
    ${r}=    POST Avaliacao Bucal Coletiva    ${acao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${c}=    GET A Convocar Saude Bucal    ${unidade}
    Should Be Equal As Integers    ${c.status_code}    200
    Validate Array Schema    ${c.json()}    ${CONVOCACAO_SAUDE_BUCAL_SCHEMA}
    Should Be Equal    ${c.json()}[0][pacienteId]    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal Coletiva - Sem Lista Nominal - Status Code 422
    [Documentation]    Without the participants list, there is no individual assessment.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}    ${paciente}    ${unidade}    ${nome}=    Seed Acao Bucal Coletiva    SAUDE_BUCAL    ${False}
    ${quantidade}=    Create Dictionary    quantidadeAtendida=${25}
    POST Realizacao Acao    ${acao}    ${quantidade}
    ${resultado}=    Create Dictionary    pacienteId=${paciente}    risco=ALTO    necessidadeTratamento=URGENCIA
    ${resultados}=    Create List    ${resultado}
    ${payload}=    Create Dictionary    resultados=${resultados}
    ${r}=    POST Avaliacao Bucal Coletiva    ${acao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal Coletiva - By HTTP Status Code
    [Documentation]    Executes the SB5 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-detalhe'    Saude Bucal Coletiva - Detalhe - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-programa'    Saude Bucal Coletiva - Programa Sem Odontologia - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-atividade'    Saude Bucal Coletiva - Sem Atividade - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-avaliacao'    Saude Bucal Coletiva - Avaliacao E A Convocar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-lista'    Saude Bucal Coletiva - Sem Lista Nominal - Status Code 422
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Saude Bucal Coletiva test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
