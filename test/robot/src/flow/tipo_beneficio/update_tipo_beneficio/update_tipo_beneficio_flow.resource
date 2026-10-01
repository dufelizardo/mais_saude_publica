*** Settings ***
Library    String
Resource    ../../../pom/tipo_beneficio/update_tipo_beneficio/update_tipo_beneficio_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Edição do catálogo (ADR-0083). Cria um item novo pela API e o edita.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Tipo Beneficio
    [Documentation]    Creates a fresh item with a random-suffixed name and returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Tipo Beneficio ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    custeio=EMPRESA
    ${criado}=    POST Create Tipo Beneficio    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Tipo Beneficio
    ${uuid}=    Evaluate    [i['uuid'] for i in $lista.json() if i['nome'] == $nome][0]
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Tipo Beneficio - Status Code 200
    [Documentation]    Edits a fresh item (ADR-0083), expecting HTTP 200 and the new values on GET by id.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Tipo Beneficio
    ${payload}=    Create Dictionary    nome=${nome} editado    custeio=COMPARTILHADO
    ${response}=    PATCH Update Tipo Beneficio    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lido}=    GET Get Tipo Beneficio By Id    ${uuid}
    Should Be Equal As Strings    ${lido.json()}[nome]    ${nome} editado
    Should Be Equal As Strings    ${lido.json()}[custeio]    COMPARTILHADO
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Tipo Beneficio - Status Code 404
    [Documentation]    Edits a random, non-existent item, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    nome=Robot inexistente    custeio=EMPRESA
    ${response}=    PATCH Update Tipo Beneficio    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tipo Beneficio - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the Update Tipo Beneficio scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Tipo Beneficio - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Tipo Beneficio - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Tipo Beneficio test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
