*** Settings ***
Resource    ../../../flow/evolucao_enfermagem/create_evolucao_enfermagem/create_evolucao_enfermagem_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
EVOLUCAO ENFERMAGEM - CREATE - POST
    [Documentation]    Test scenario for creating an EvolucaoEnfermagem (POST /api/v1/evolucao-enfermagem/)
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Evolucao Enfermagem - Create - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

EVOLUCAO ENFERMAGEM - CREATE - POST - Atendimento Inexistente
    [Documentation]    Test scenario for creating an EvolucaoEnfermagem pointing at an atendimentoId that
    ...    doesn't exist, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Evolucao Enfermagem - Status Code 404 - Atendimento Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

EVOLUCAO ENFERMAGEM - CREATE - POST - Profissional Inexistente
    [Documentation]    Test scenario for creating an EvolucaoEnfermagem pointing at a
    ...    profissionalMatricula that doesn't exist, expecting HTTP 404 (ver ADR-0034).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    POST Create Evolucao Enfermagem - Status Code 404 - Profissional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
