*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/laboratorio/create_exame_laboratorial/create_exame_laboratorial_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Catálogo de exames do laboratório (ADR-0093).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Exame Laboratorial
    [Documentation]    Creates a numeric blood exam (reference 12-16 g/dL) and returns uuid and nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Exame ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    material=SANGUE    tipoResultado=NUMERICO    unidadeMedida=g/dL    referenciaMinima=${12}    referenciaMaxima=${16}    preparo=Jejum de 4 horas
    ${criado}=    POST Create Exame Laboratorial    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Exame Laboratorial
    ${uuid}=    Evaluate    [e['uuid'] for e in $lista.json() if e['nome'] == $nome][0]
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Exame Laboratorial - Status Code 201
    [Documentation]    Creates an exam and validates the catalog schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Exame Laboratorial
    ${lista}=    GET List Exame Laboratorial
    Validate Array Schema    ${lista.json()}    ${EXAME_LABORATORIAL_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Exame Laboratorial - Status Code 409 - Nome Repetido
    [Documentation]    Creates the same name twice, expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Exame Laboratorial
    ${payload}=    Create Dictionary    nome=${nome}    material=URINA    tipoResultado=TEXTO
    ${response}=    POST Create Exame Laboratorial    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Exame Laboratorial - Status Code 400 - Faixa Invertida
    [Documentation]    Sends minimum above maximum, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    nome=Robot Exame Invertido    material=SANGUE    tipoResultado=NUMERICO    referenciaMinima=${20}    referenciaMaxima=${10}
    ${response}=    POST Create Exame Laboratorial    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Exame Laboratorial - Status Code 200
    [Documentation]    Takes an exam out of use.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Exame Laboratorial
    ${payload}=    Create Dictionary    nome=${nome}    material=SANGUE    tipoResultado=NUMERICO    ativo=${False}
    ${response}=    PATCH Update Exame Laboratorial    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Laboratorial - POST PATCH - By HTTP Status Code
    [Documentation]    Executes the Exame Laboratorial - POST PATCH scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Exame Laboratorial - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Exame Laboratorial - Status Code 409 - Nome Repetido
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Exame Laboratorial - Status Code 400 - Faixa Invertida
    ...    ELSE IF    '${meuHTTP}' == '200'    PATCH Update Exame Laboratorial - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Exame Laboratorial - POST PATCH test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
