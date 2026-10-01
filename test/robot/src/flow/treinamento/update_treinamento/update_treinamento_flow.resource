*** Settings ***
Library    String
Resource    ../../../pom/treinamento/update_treinamento/update_treinamento_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Edição do catálogo (ADR-0083). Cria um item novo pela API e o edita.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Treinamento
    [Documentation]    Creates a fresh item with a random-suffixed name and returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Treinamento ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    cargaHoraria=${8}    validadeMeses=${12}    obrigatorio=${True}
    ${criado}=    POST Create Treinamento    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Treinamento
    ${uuid}=    Evaluate    [i['uuid'] for i in $lista.json() if i['nome'] == $nome][0]
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Treinamento - Status Code 200
    [Documentation]    Edits a fresh item (ADR-0083), expecting HTTP 200 and the new values on GET by id.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Treinamento
    ${payload}=    Create Dictionary    nome=${nome} editado    cargaHoraria=${16}    validadeMeses=${24}    obrigatorio=${False}
    ${response}=    PATCH Update Treinamento    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lido}=    GET Get Treinamento By Id    ${uuid}
    Should Be Equal As Strings    ${lido.json()}[nome]    ${nome} editado
    Should Be Equal As Strings    ${lido.json()}[cargaHoraria]    16
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Treinamento - Status Code 404
    [Documentation]    Edits a random, non-existent item, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    nome=Robot inexistente    cargaHoraria=${8}    validadeMeses=${12}    obrigatorio=${True}
    ${response}=    PATCH Update Treinamento    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Treinamento - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the Update Treinamento scenario for the status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Treinamento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Treinamento - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Treinamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
