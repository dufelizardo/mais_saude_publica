*** Settings ***
Resource    ../../../pom/paciente/update_paciente/update_paciente_pom.resource
Resource    ../create_paciente/create_paciente_flow.resource
Resource    ../get_paciente_by_id/get_paciente_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Paciente's PATCH replaces every editable field at once (mesmo contrato do Setor) — setting ativo=False
    aqui dobra como o cenário de "inativar" do domínio, já que não existe um endpoint dedicado de
    des-habilitar para Paciente (Paciente não tem vínculo empregatício/data de desligamento, ao contrário
    do Profissional — ver ADR-0040).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Paciente
    [Documentation]    Calls the PATCH Update Paciente keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Paciente    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Paciente - Status Code 200
    [Documentation]    Seeds a fresh Paciente and updates its nome, marking it inativo (ativo=False) — covers
    ...    both "editar cadastro" and "inativar" em um único cenário.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${payload}=    Build Paciente Payload    Robot Teste Paciente Atualizado    ${cpf}    ${cartaoSus}    ${False}

    ${response}=    Call PATCH Update Paciente    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Paciente By Id    ${uuid}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['nome']}    Robot Teste Paciente Atualizado
    Should Be Equal    ${getResponse.json()['ativo']}    ${False}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Paciente - Status Code 404
    [Documentation]    Attempts to update a Paciente identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${payload}=    Build Paciente Payload    Robot Teste Paciente Inexistente    ${suffix}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Paciente    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Paciente - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Paciente test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Paciente - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Paciente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Paciente test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
