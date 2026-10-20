*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/vigilancia_pom.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Agravo
    [Documentation]    Returns the uuid of an agravo by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Agravos
    ${ids}=    Evaluate    [a["uuid"] for a in $response.json() if a["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Agravos - Status Code 200
    [Documentation]    The catalog brings the national list, with the dengue form.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Agravos
    Should Be Equal As Integers    ${response.status_code}    200
    ${dengue}=    Evaluate    [a for a in $response.json() if a["codigo"] == "DENGUE"][0]
    Validate Object Schema    ${dengue}    ${AGRAVO_SCHEMA}
    Should Be Equal As Integers    ${dengue}[fichaVersao]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Notificacao - Status Code 201
    [Documentation]    Notifies dengue with the form answers.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    Validate Object Schema    ${n.json()}    ${NOTIFICACAO_SCHEMA}
    Should Be Equal    ${n.json()}[situacao]    NOTIFICADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Ficha Obrigatoria - Status Code 400
    [Documentation]    A required field of the form must be answered.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO
    ${response}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Ciclo - Status Code 200
    [Documentation]    Investigate, classify as confirmed and close.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    ${inv}=    Create Dictionary    exposicao=Vizinho com dengue.
    ${r1}=    POST Acao Notificacao    ${notificacao}    investigacao    ${inv}
    Should Be Equal    ${r1.json()}[situacao]    EM_INVESTIGACAO
    ${cls}=    Create Dictionary    classificacao=CONFIRMADO    criterio=LABORATORIAL
    ${r2}=    POST Acao Notificacao    ${notificacao}    classificacao    ${cls}
    Should Be Equal As Integers    ${r2.status_code}    201
    ${enc}=    Create Dictionary    evolucao=CURA
    ${r3}=    POST Acao Notificacao    ${notificacao}    encerramento    ${enc}
    Should Be Equal As Integers    ${r3.status_code}    200
    Should Be Equal    ${r3.json()}[classificacaoFinal]    CONFIRMADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Criterio Nao Aceito - Status Code 422
    [Documentation]    Dengue does not accept the clinical criterion.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    ${cls}=    Create Dictionary    classificacao=CONFIRMADO    criterio=CLINICO
    ${r}=    POST Acao Notificacao    ${notificacao}    classificacao    ${cls}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Encerrar Sem Final - Status Code 422
    [Documentation]    Closing needs a final classification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    ${enc}=    Create Dictionary    evolucao=CURA
    ${r}=    POST Acao Notificacao    ${notificacao}    encerramento    ${enc}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Negativa - Status Code 201
    [Documentation]    Declares the previous week; again is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    ${r1}=    POST Notificacao Negativa    ${payload}
    Should Be Equal As Integers    ${r1.status_code}    201
    ${r2}=    POST Notificacao Negativa    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Agravo Local - Status Code 201
    [Documentation]    Creates a local agravo; the same code again is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    ${criterios}=    Create List    CLINICO
    ${payload}=    Create Dictionary    codigo=ROBOT_${sufixo}    nome=Agravo local ${sufixo}    periodicidade=SEMANAL    prazoEncerramentoDias=${30}    criterios=${criterios}
    ${r1}=    POST Agravo    ${payload}
    Should Be Equal As Integers    ${r1.status_code}    201
    ${r2}=    POST Agravo    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - Boletim - Status Code 200
    [Documentation]    The bulletin only has aggregates.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    ${notificacao}=    Set Variable    ${n.json()}[uuid]
    ${response}=    GET Boletim
    Should Be Equal As Integers    ${response.status_code}    200
    ${codigos}=    Evaluate    [l["agravoCodigo"] for l in $response.json()["linhas"]]
    Should Contain    ${codigos}    DENGUE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia - By HTTP Status Code
    [Documentation]    Executes the V1 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-agravos'    Vigilancia - Agravos - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-notificacao'    Vigilancia - Notificacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-ficha'    Vigilancia - Ficha Obrigatoria - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-ciclo'    Vigilancia - Ciclo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-criterio'    Vigilancia - Criterio Nao Aceito - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-encerrar'    Vigilancia - Encerrar Sem Final - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-negativa'    Vigilancia - Negativa - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '201-agravo'    Vigilancia - Agravo Local - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-boletim'    Vigilancia - Boletim - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Vigilancia test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
