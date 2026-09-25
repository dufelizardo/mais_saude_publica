*** Settings ***
Resource    ../../../flow/consulta/create_consulta/create_consulta_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
CONSULTA - CREATE - POST
    [Documentation]    Test scenario for creating a Consulta (POST /api/v1/consulta/) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Consulta - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CONSULTA - CREATE - POST - Atendimento Inexistente
    [Documentation]    Test scenario for creating a Consulta pointing at an atendimentoId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Consulta - Status Code 404 - Atendimento Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CONSULTA - CREATE - POST - Profissional Inexistente
    [Documentation]    Test scenario for creating a Consulta pointing at a profissionalMatricula that
    ...    doesn't exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Consulta - Status Code 404 - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
