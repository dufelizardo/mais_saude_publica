*** Settings ***
Resource    ../../../pom/necessidade_de_pessoal/update_necessidade_de_pessoal/update_necessidade_de_pessoal_pom.resource
Resource    ../create_necessidade_de_pessoal/create_necessidade_de_pessoal_flow.resource
Resource    ../get_necessidade_de_pessoal_by_id/get_necessidade_de_pessoal_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    PATCH substitui os campos editáveis por inteiro (mesmo contrato de Setor/ProcessoAdministrativo) —
    não altera dataRegistro nem vagaAssociada (ver ADR-0036).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Necessidade De Pessoal
    [Documentation]    Calls the PATCH Update Necessidade De Pessoal keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Necessidade De Pessoal    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Necessidade De Pessoal - Status Code 200
    [Documentation]    Seeds a fresh Necessidade De Pessoal and updates sua quantidade/jornada, confirmando via
    ...    GET.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${necessidadeId}    ${unidadeId}    ${cargoId}=    Seed A Necessidade De Pessoal
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeId}    ${cargoId}    7    20

    ${response}=    Call PATCH Update Necessidade De Pessoal    ${necessidadeId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Necessidade De Pessoal By Id    ${necessidadeId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Integers    ${getResponse.json()['quantidade']}    7
    Should Be Equal As Integers    ${getResponse.json()['jornadaSemanalHoras']}    20

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Necessidade De Pessoal - Status Code 404
    [Documentation]    Attempts to update a Necessidade De Pessoal identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${cargoId}=    Seed A Cargo Uuid
    ${payload}=    Build Necessidade De Pessoal Payload    ${unidadeId}    ${cargoId}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Necessidade De Pessoal    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Necessidade De Pessoal - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Necessidade De Pessoal test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Necessidade De Pessoal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Necessidade De Pessoal - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Necessidade De Pessoal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
