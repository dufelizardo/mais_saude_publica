*** Settings ***
Resource    ../../../flow/atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../../flow/atendimento/get_atendimento_by_id/get_atendimento_by_id_flow.resource
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

ATENDIMENTO - CREATE - POST - Com Agendamento
    [Documentation]    Test scenario for creating an Atendimento linked to a pre-existing Agendamento,
    ...    expecting HTTP 201 and the GET response to carry the matching agendamentoUuid (ver ADR-0042).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${agendamentoId}=    POST Create Atendimento - Status Code 201 - Com Agendamento
    ${getResponse}=    Call GET Get Atendimento By Id    ${atendimentoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['agendamentoUuid']}    ${agendamentoId}
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
