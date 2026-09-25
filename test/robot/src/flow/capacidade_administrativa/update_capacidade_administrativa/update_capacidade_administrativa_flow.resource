*** Settings ***
Resource    ../../../pom/capacidade_administrativa/update_capacidade_administrativa/update_capacidade_administrativa_pom.resource
Resource    ../create_capacidade_administrativa/create_capacidade_administrativa_flow.resource
Resource    ../get_capacidade_administrativa_by_id/get_capacidade_administrativa_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    PATCH substitui os campos editáveis por inteiro (mesmo contrato de Setor/Vaga/Cargo). Marcar
    ativo=False aqui dobra como o cenário de "desativar" — não existe endpoint dedicado.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Capacidade Administrativa
    [Documentation]    Calls the PATCH Update Capacidade Administrativa keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Capacidade Administrativa    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Capacidade Administrativa - Status Code 200
    [Documentation]    Seeds a fresh Capacidade Administrativa and updates its nome, marking it inativo
    ...    (ativo=False) — covers both "editar campos" and "desativar" in a single scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${codigo}=    Seed A Capacidade Administrativa
    ${payload}=    Build Capacidade Administrativa Payload
    ...    ${codigo}    Robot Teste Capacidade Atualizada    ${EMPTY}    ${False}

    ${response}=    Call PATCH Update Capacidade Administrativa    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Capacidade Administrativa By Id    ${uuid}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['nome']}    Robot Teste Capacidade Atualizada
    Should Be Equal    ${getResponse.json()['ativo']}    ${False}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Capacidade Administrativa - Status Code 404
    [Documentation]    Attempts to update a Capacidade Administrativa identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Build Capacidade Administrativa Payload    ROBOT-CAP-INEXISTENTE    Robot Teste Capacidade
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Capacidade Administrativa    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Capacidade Administrativa - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Capacidade Administrativa test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Capacidade Administrativa - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Capacidade Administrativa - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Capacidade Administrativa test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
