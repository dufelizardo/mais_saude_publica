*** Settings ***
Resource    ../../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Laboratório: laudo em PDF com assinatura eletrônica interna, verificação pública e insumos do Estoque (ADR-0183).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${LAUDO_PEDIDO_ENDPOINT}    /pedido-exame
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Laudo Pdf
    [Documentation]    Emits (or reuses) the PDF report of the order.
    [Arguments]    ${pedidoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${LAUDO_PEDIDO_ENDPOINT}/${pedidoId}/laudo.pdf    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Emissoes Laudo
    [Documentation]    Lists the report emissions of the order.
    [Arguments]    ${pedidoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}${LAUDO_PEDIDO_ENDPOINT}/${pedidoId}/emissoes    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Verificar Laudo
    [Documentation]    Public verification by the code, without login.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/publico/laudo/${codigo}    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PUT Insumos Exame
    [Documentation]    Replaces the supplies of an exam.
    [Arguments]    ${exameId}    ${lista}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${response}=    PUT On Session    msp_session    ${BASE_URL}/exame-laboratorial/${exameId}/insumos    json=${lista}    headers=${headers}    expected_status=ANY
    Log    ${response.status_code}
    Log    ${response.text}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Conferir Consumo Sem Saldo
    [Documentation]    Marks a consumption without balance as checked.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/estoque/consumo-sem-saldo/${uuid}/conferencia    expected_status=ANY
    Log    ${response.status_code}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
