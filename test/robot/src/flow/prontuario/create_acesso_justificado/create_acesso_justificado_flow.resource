*** Settings ***
Resource    ../../../pom/prontuario/create_acesso_justificado/create_acesso_justificado_pom.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Acesso justificado ao prontuário (ADR-0076): motivo e texto de pelo menos 20 caracteres, válido por 4 horas
    só para o paciente e o usuário. O registro não depende do toggle do vínculo, então roda em qualquer ambiente.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Acesso Justificado Payload
    [Documentation]    Builds the payload for a justified access.
    [Arguments]    ${motivo}=EMERGENCIA
    ...    ${justificativa}=Paciente chegou desacordado, preciso do histórico de alergias
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    motivo=${motivo}
    ...    justificativa=${justificativa}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Call POST Create Acesso Justificado
    [Documentation]    Calls the POST Create Acesso Justificado keyword from the POM.
    [Arguments]    ${pacienteId}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Acesso Justificado    ${pacienteId}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Acesso Justificado - Status Code 201
    [Documentation]    Registers a justified access for a fresh Paciente, expecting HTTP 201 with the motivo and
    ...    an expiration time, and no justification text in the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Acesso Justificado Payload
    ${response}=    Call POST Create Acesso Justificado    ${pacienteId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${body}=    Set Variable    ${response.json()}
    Should Be Equal As Strings    ${body['pacienteUuid']}    ${pacienteId}
    Should Be Equal As Strings    ${body['motivo']}    EMERGENCIA
    Should Not Be Empty    ${body['uuid']}
    Should Not Be Empty    ${body['expiraEm']}
    Dictionary Should Not Contain Key    ${body}    justificativa

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Acesso Justificado - Status Code 400
    [Documentation]    Sends a justification shorter than the minimum, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Acesso Justificado Payload    justificativa=curto demais
    ${response}=    Call POST Create Acesso Justificado    ${pacienteId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Acesso Justificado - Status Code 404
    [Documentation]    Registers a justified access for a random, non-existent pacienteId, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Build Acesso Justificado Payload
    ${response}=    Call POST Create Acesso Justificado    ${pacienteIdInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prontuario - Create Acesso Justificado - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Acesso Justificado test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Acesso Justificado - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Acesso Justificado - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Acesso Justificado - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Acesso Justificado test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
