*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/procedimento_regulado/update_procedimento_regulado/update_procedimento_regulado_pom.resource
Resource    ../create_procedimento_regulado/create_procedimento_regulado_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Edição do catálogo: tirar de uso não afeta as solicitações já feitas.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PATCH Update Procedimento Regulado - Status Code 200
    [Documentation]    Renames the item, takes it out of use and reads it back.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Procedimento Regulado
    ${payload}=    Create Dictionary    nome=${nome} Editado    tipo=PROCEDIMENTO    ativo=${False}
    ${response}=    PATCH Update Procedimento Regulado    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}
    ${lido}=    GET Get Procedimento Regulado By Id    ${uuid}
    Validate Object Schema    ${lido.json()}    ${PROCEDIMENTO_REGULADO_SCHEMA}
    Should Be Equal    ${lido.json()}[nome]    ${nome} Editado
    Should Not Be True    ${lido.json()}[ativo]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Procedimento Regulado - Status Code 404
    [Documentation]    Edits a random uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    nome=Robot Fantasma    tipo=EXAME
    ${response}=    PATCH Update Procedimento Regulado    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Regulado - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the Procedimento Regulado - Update - PATCH scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Procedimento Regulado - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Procedimento Regulado - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Procedimento Regulado - Update - PATCH test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
