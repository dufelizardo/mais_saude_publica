*** Settings ***
Resource    ../../../flow/setor/create_setor/create_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
SETOR - CREATE - POST
    [Documentation]    Test scenario for creating a Setor (POST /api/v1/setor/) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Setor - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

SETOR - CREATE - POST - Com Responsavel
    [Documentation]    Test scenario for creating a Setor with a matriculaResponsavel, expecting HTTP 201 and
    ...    the GET response to carry responsavelMatricula/responsavelNome (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Setor - Status Code 201 - Com Responsavel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

SETOR - CREATE - POST - Responsavel Inexistente
    [Documentation]    Test scenario for creating a Setor pointing at a matriculaResponsavel that doesn't exist,
    ...    expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Setor - Status Code 404 - Responsavel Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
