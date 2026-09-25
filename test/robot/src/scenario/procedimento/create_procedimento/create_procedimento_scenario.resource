*** Settings ***
Resource    ../../../flow/procedimento/create_procedimento/create_procedimento_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PROCEDIMENTO - CREATE - POST
    [Documentation]    Test scenario for creating a Procedimento (POST /api/v1/procedimento/) on the Mais
    ...    Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Procedimento - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROCEDIMENTO - CREATE - POST - Consulta Inexistente
    [Documentation]    Test scenario for creating a Procedimento pointing at a consultaId that doesn't
    ...    exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Procedimento - Status Code 404 - Consulta Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PROCEDIMENTO - CREATE - POST - Profissional Inexistente
    [Documentation]    Test scenario for creating a Procedimento pointing at a profissionalMatricula that
    ...    doesn't exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Procedimento - Status Code 404 - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
