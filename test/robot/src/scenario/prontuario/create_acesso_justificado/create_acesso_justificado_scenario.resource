*** Settings ***
Resource    ../../../flow/prontuario/create_acesso_justificado/create_acesso_justificado_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PRONTUARIO - CREATE ACESSO JUSTIFICADO - POST
    [Documentation]    Test scenario for registering a justified access to a Paciente's Prontuário
    ...    (POST /api/v1/prontuario/{pacienteId}/acesso-justificado) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Prontuario - Create Acesso Justificado - POST - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
