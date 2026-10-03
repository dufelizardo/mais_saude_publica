*** Settings ***
Library    String
Resource    ../../../pom/ciclo_avaliacao/update_ciclo_avaliacao/update_ciclo_avaliacao_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Edição do catálogo (ADR-0083). Cria um item novo pela API e o edita.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Ciclo Avaliacao
    [Documentation]    Creates a fresh item with a random-suffixed name and returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Ciclo Avaliacao ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    dataInicio=2027-01-01    dataFim=2027-06-30
    ${criado}=    POST Create Ciclo Avaliacao    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Ciclo Avaliacao
    ${uuid}=    Evaluate    [i['uuid'] for i in $lista.json() if i['nome'] == $nome][0]
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Ciclo Avaliacao - Status Code 200
    [Documentation]    Edits a fresh item (ADR-0083), expecting HTTP 200 and the new values on GET by id.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Ciclo Avaliacao
    ${payload}=    Create Dictionary    nome=${nome} editado    dataInicio=2027-02-01    dataFim=2027-07-31
    ${response}=    PATCH Update Ciclo Avaliacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lido}=    GET Get Ciclo Avaliacao By Id    ${uuid}
    Should Be Equal As Strings    ${lido.json()}[nome]    ${nome} editado
    Should Be Equal As Strings    ${lido.json()}[dataFim]    2027-07-31
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Ciclo Avaliacao - Status Code 404
    [Documentation]    Edits a random, non-existent item, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    nome=Robot inexistente    dataInicio=2027-01-01    dataFim=2027-06-30
    ${response}=    PATCH Update Ciclo Avaliacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Ciclo Avaliacao - Status Code 400
    [Documentation]    Edits with the end before the start, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Ciclo Avaliacao
    ${payload}=    Create Dictionary    nome=${nome}    dataInicio=2027-06-30    dataFim=2027-01-01
    ${response}=    PATCH Update Ciclo Avaliacao    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciclo Avaliacao - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the Update Ciclo Avaliacao scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Ciclo Avaliacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Ciclo Avaliacao - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '400'    PATCH Update Ciclo Avaliacao - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Ciclo Avaliacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
