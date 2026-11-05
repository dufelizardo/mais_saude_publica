*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/vigilancia_ambiental_pom.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Qualidade da água e risco ambiental (ADR-0164).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Ponto Agua
    [Documentation]    A sampling point; returns the uuid.
    [Arguments]    ${tipo}=REDE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${payload}=    Create Dictionary    nome=Robot Ponto ${sufixo}    tipo=${tipo}    local=Praça Robot
    ${r}=    POST Ponto Agua    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Risco Ambiental
    [Documentation]    An active environmental risk; returns the response.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${payload}=    Create Dictionary    tipo=DESCARTE_IRREGULAR    descricao=Entulho no córrego    local=Córrego Robot ${sufixo}    populacaoExposta=${200}
    ${r}=    POST Risco Ambiental    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Ponto - Status Code 201
    [Documentation]    The sampling point is registered.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Ponto Agua
    ${r}=    GET Pontos Agua
    ${ids}=    Evaluate    [p['uuid'] for p in $r.json()]
    List Should Contain Value    ${ids}    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Amostra Conforme - Status Code 201
    [Documentation]    A sample within the standard.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ponto}=    Seed Ponto Agua
    ${payload}=    Create Dictionary    pontoId=${ponto}    ecoli=${False}    coliformesTotais=${False}    cloroResidual=${0.8}    turbidez=${0.5}    fluor=${0.7}
    ${r}=    POST Amostra Agua    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${AMOSTRA_AGUA_SCHEMA}
    Should Be True    ${r.json()}[conforme]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Amostra Fora Do Padrao - Status Code 201
    [Documentation]    Low chlorine and E. coli fail the standard.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ponto}=    Seed Ponto Agua
    ${payload}=    Create Dictionary    pontoId=${ponto}    ecoli=${True}    cloroResidual=${0.1}
    ${r}=    POST Amostra Agua    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Not Be True    ${r.json()}[conforme]
    Length Should Be    ${r.json()}[naoConformidades]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Amostra Vazia - Status Code 400
    [Documentation]    At least one parameter.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ponto}=    Seed Ponto Agua
    ${payload}=    Create Dictionary    pontoId=${ponto}
    ${r}=    POST Amostra Agua    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Medida - Status Code 200
    [Documentation]    The measure for the non-compliant sample.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ponto}=    Seed Ponto Agua
    ${payload}=    Create Dictionary    pontoId=${ponto}    turbidez=${8}
    ${a}=    POST Amostra Agua    ${payload}
    ${m}=    Create Dictionary    medida=Recoleta em 48 horas
    ${r}=    POST Medida Da Amostra    ${a.json()}[uuid]    ${m}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[medida]    Recoleta em 48 horas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Medida Na Conforme - Status Code 422
    [Documentation]    A compliant sample needs no measure.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ponto}=    Seed Ponto Agua
    ${payload}=    Create Dictionary    pontoId=${ponto}    turbidez=${1}
    ${a}=    POST Amostra Agua    ${payload}
    ${m}=    Create Dictionary    medida=x
    ${r}=    POST Medida Da Amostra    ${a.json()}[uuid]    ${m}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Risco - Status Code 201
    [Documentation]    The environmental risk is born active.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    Seed Risco Ambiental
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${RISCO_AMBIENTAL_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    ATIVO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - Risco Controlado - Status Code 200
    [Documentation]    The risk is controlled; again, 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    Seed Risco Ambiental
    ${m}=    Create Dictionary    motivo=Limpeza feita
    ${c}=    POST Controle Do Risco    ${r.json()}[uuid]    ${m}
    Should Be Equal As Integers    ${c.status_code}    200
    Should Be Equal    ${c.json()}[situacao]    CONTROLADO
    ${again}=    POST Controle Do Risco    ${r.json()}[uuid]    ${m}
    Should Be Equal As Integers    ${again.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Ambiental - By HTTP Status Code
    [Documentation]    Executes the V4c scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-ponto'    Vigilancia Ambiental - Ponto - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '201-amostra-conforme'    Vigilancia Ambiental - Amostra Conforme - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '201-amostra-fora-do-padrao'    Vigilancia Ambiental - Amostra Fora Do Padrao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-amostra-vazia'    Vigilancia Ambiental - Amostra Vazia - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-medida'    Vigilancia Ambiental - Medida - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-medida-na-conforme'    Vigilancia Ambiental - Medida Na Conforme - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-risco'    Vigilancia Ambiental - Risco - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-risco-controlado'    Vigilancia Ambiental - Risco Controlado - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Vigilancia Ambiental test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
