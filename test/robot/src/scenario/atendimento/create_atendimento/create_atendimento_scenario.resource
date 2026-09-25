*** Settings ***
Resource    ../../../flow/atendimento/create_atendimento/create_atendimento_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
ATENDIMENTO - CREATE - POST
    [Documentation]    Test scenario for creating an Atendimento (POST /api/v1/atendimento/) on the Mais Saúde
    ...    Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Atendimento - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ATENDIMENTO - CREATE - POST - Paciente Inexistente
    [Documentation]    Test scenario for creating an Atendimento pointing at a pacienteId that doesn't exist,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Atendimento - Status Code 404 - Paciente Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

ATENDIMENTO - CREATE - POST - Profissional Inexistente
    [Documentation]    Test scenario for creating an Atendimento pointing at a profissionalMatricula that
    ...    doesn't exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Atendimento - Status Code 404 - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
