*** Settings ***
Resource    ../../../flow/paciente/get_paciente_by_cartao_sus/get_paciente_by_cartao_sus_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PACIENTE - GET BY CARTAO SUS - GET
    [Documentation]    Test scenario for fetching Pacientes by cartaoSus (GET /api/v1/paciente/cartao-sus/{cartaoSus})
    ...    on the Mais Saúde Pública API.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Paciente - Get By Cartao Sus - GET - By HTTP Status Code    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
