*** Settings ***
Resource    ../../../flow/responsabilidade_administrativa/get_responsaveis_setor/get_responsaveis_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
RESPONSABILIDADE ADMINISTRATIVA - RESPONSAVEIS DO SETOR - GET
    [Documentation]    Test scenario for GET .../setor/{setorId} (ADR-0083).
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Responsabilidade Administrativa - Responsaveis Do Setor - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
