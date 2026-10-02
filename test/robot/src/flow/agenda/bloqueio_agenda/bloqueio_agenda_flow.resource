*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/agenda/bloqueio_agenda/bloqueio_agenda_pom.resource
Resource    ../create_bloco_agenda/create_bloco_agenda_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Bloqueio tira as vagas do período; removido, elas voltam (ADR-0091).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Bloqueio Agenda - Status Code 201
    [Documentation]    Blocks 08:00-09:00 (two slots left), removes the block-out (four slots back).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${unidadeId}    ${segunda}=    Seed A Bloco Agenda
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    inicio=${segunda}T08:00:00    fim=${segunda}T09:00:00    motivo=REUNIAO
    ${response}=    POST Create Bloqueio Agenda    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${vagas}=    GET Vagas Agenda    ${matricula}    ${unidadeId}    ${segunda}
    Length Should Be    ${vagas.json()}    2
    ${uuid}=    Evaluate    $response.json()['details'].split(',')[0].replace('Id: ', '').strip()
    ${removido}=    DELETE Bloqueio Agenda    ${uuid}
    Should Be Equal As Integers    ${removido.status_code}    200
    ${depois}=    GET Vagas Agenda    ${matricula}    ${unidadeId}    ${segunda}
    Length Should Be    ${depois.json()}    4
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Bloqueio Agenda - Status Code 400 - Sem Profissional Nem Unidade
    [Documentation]    Blocks without professional or unit, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    inicio=2030-01-07T08:00:00    fim=2030-01-07T09:00:00    motivo=OUTRO
    ${response}=    POST Create Bloqueio Agenda    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Bloqueio Agenda - Status Code 404 - Remover Inexistente
    [Documentation]    Removes a random block-out, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    DELETE Bloqueio Agenda    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Bloqueio Agenda - POST DELETE - By HTTP Status Code
    [Documentation]    Executes the Bloqueio Agenda - POST DELETE scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Bloqueio Agenda - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    Bloqueio Agenda - Status Code 400 - Sem Profissional Nem Unidade
    ...    ELSE IF    '${meuHTTP}' == '404'    Bloqueio Agenda - Status Code 404 - Remover Inexistente
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Bloqueio Agenda - POST DELETE test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
