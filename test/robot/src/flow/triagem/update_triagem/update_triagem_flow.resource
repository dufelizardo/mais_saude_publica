*** Settings ***
Resource    ../../../pom/triagem/update_triagem/update_triagem_pom.resource
Resource    ../create_triagem/create_triagem_flow.resource
Resource    ../get_triagem_by_id/get_triagem_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Triagem's PATCH replaces every editable field at once (mesmo contrato do Setor/Atendimento/Consulta,
    ver ADR-0047) — corrigir a classificação de risco é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Triagem
    [Documentation]    Calls the PATCH Update Triagem keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Triagem    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Triagem - Status Code 200
    [Documentation]    Seeds a fresh Triagem and corrects its classificacaoRisco.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${triagemId}    ${atendimentoId}    ${matricula}=    Seed Uma Triagem
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}    classificacaoRisco=VERMELHO

    ${response}=    Call PATCH Update Triagem    ${triagemId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Triagem By Id    ${triagemId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['classificacaoRisco']}    VERMELHO

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Triagem - Status Code 404
    [Documentation]    Attempts to update a Triagem identified by a random, non-existent uuid, expecting
    ...    HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Triagem    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Triagem - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Triagem test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Triagem - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Triagem - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Triagem test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
