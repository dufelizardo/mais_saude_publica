*** Settings ***
Resource    ../../../flow/prontuario/get_prontuario_by_paciente_id/get_prontuario_by_paciente_id_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PRONTUARIO - GET BY PACIENTE ID - GET
    [Documentation]    Test scenario for fetching a Paciente's consolidated Prontuário
    ...    (GET /api/v1/prontuario/{pacienteId}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Prontuario - Get By Paciente Id - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PRONTUARIO - GET BY PACIENTE ID - GET - Sem Atendimentos
    [Documentation]    Test scenario for fetching the Prontuário of a Paciente with no Atendimento yet,
    ...    expecting HTTP 200 with an empty atendimentos list.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    GET Get Prontuario By Paciente Id - Status Code 200 - Sem Atendimentos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
