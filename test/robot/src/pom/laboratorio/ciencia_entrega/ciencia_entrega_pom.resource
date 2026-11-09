*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Laboratório: ciência de quem pediu, comunicação do valor crítico e entrega do resultado ao paciente (ADR-0181).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${CIENCIA_PEDIDO_EXAME_ENDPOINT}    /pedido-exame
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Para Ciencia
    [Documentation]    Released results waiting for the requester's acknowledgment; without login, the registration is required.
    [Arguments]    ${matricula}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${params}=    Create Dictionary
    IF    '${matricula}' != ''    Set To Dictionary    ${params}    matricula=${matricula}
    ${response}=    GET On Session    msp_session    ${BASE_URL}${CIENCIA_PEDIDO_EXAME_ENDPOINT}/para-ciencia    params=${params}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Criticos A Comunicar
    [Documentation]    Released critical results without communication nor acknowledgment, in the labs of the scope.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${CIENCIA_PEDIDO_EXAME_ENDPOINT}/criticos-a-comunicar    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Ciencia Do Pedido
    [Documentation]    Acknowledges every released result of the order still pending.
    [Arguments]    ${pedidoId}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${CIENCIA_PEDIDO_EXAME_ENDPOINT}/${pedidoId}/ciencia    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Entrega Resultado
    [Documentation]    Registers the delivery of the result to the patient.
    [Arguments]    ${pedidoId}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    Post On Session    msp_session    ${BASE_URL}${CIENCIA_PEDIDO_EXAME_ENDPOINT}/${pedidoId}/entrega    json=${payload}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
