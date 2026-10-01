*** Settings ***
Resource    ../../../flow/agendamento/get_agendamentos_by_paciente/get_agendamentos_by_paciente_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
AGENDAMENTO - GET BY PACIENTE - GET
    [Documentation]    Test scenario for listing the Agendamentos of a Paciente
    ...    (GET /api/v1/agendamento/paciente/{pacienteId}) on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Agendamento - Get By Paciente - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
