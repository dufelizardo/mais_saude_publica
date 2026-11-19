*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/financeiro/custos_pom.resource
Resource    ../setor/create_setor/create_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os centros de custo, a apuração da competência e os indicadores de custo (ADR-0220).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Centro Geral Ativo
    [Documentation]    The uuid of an active general cost center (for the standalone settlement).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Centros Custo
    Should Be Equal As Integers    ${r.status_code}    200
    ${ids}=    Evaluate    [c['uuid'] for c in $r.json() if c['geral'] and c['ativo']]
    Should Not Be Empty    ${ids}
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Competencia Livre
    [Documentation]    A past month (1950 to 2019) still not calculated, so each run starts clean.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FOR    ${i}    IN RANGE    20
        ${c}=    Evaluate    '%d-%02d' % (random.randint(1950, 2019), random.randint(1, 12))    modules=random
        ${r}=    GET Apuracao Custo    ${c}
        IF    ${r.status_code} == 404    RETURN    ${c}
    END
    Fail    Nenhuma competência livre em 20 tentativas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Custos - Centros - Status Code 201 409 404 E 422
    [Documentation]    The general center of each unit; the sector center (201, then 409); unknown sector 404; the general one is not deactivated (422).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setor}    ${codigo}    ${unidade}=    Seed A Setor    ASSISTENCIAL
    ${r}=    GET Centros Custo
    Should Be Equal As Integers    ${r.status_code}    200
    ${geral}=    Evaluate    [c for c in $r.json() if c['unidadeId'] == $unidade and c['geral']]
    Length Should Be    ${geral}    1
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    setorId=${uuid}
    ${r}=    POST Centro Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    404
    ${payload}=    Create Dictionary    setorId=${setor}
    ${r}=    POST Centro Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[tipo]    PRODUTIVO
    Should Start With    ${r.json()}[codigo]    CC-
    ${r}=    POST Centro Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    ${payload}=    Create Dictionary    nome=Geral    tipo=PRODUTIVO    ativo=${False}
    ${r}=    PUT Centro Custo    ${geral}[0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Custos - Apuracao - Status Code 422 400 200 E 404
    [Documentation]    The current month is 422; a bad format 400; a past month is calculated, recalculated, closed (200) and then frozen (422).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atual}=    Get Current Date    UTC    - 3 hours    result_format=%Y-%m
    ${payload}=    Create Dictionary    competencia=${atual}
    ${r}=    POST Apuracao Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${payload}=    Create Dictionary    competencia=2026-13
    ${r}=    POST Apuracao Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${c}=    Competencia Livre
    ${r}=    GET Indicadores Custo    ${c}
    Should Be Equal As Integers    ${r.status_code}    404
    ${payload}=    Create Dictionary    competencia=${c}
    ${r}=    POST Apuracao Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ABERTA
    ${r}=    POST Apuracao Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${id}=    Set Variable    ${r.json()}[uuid]
    ${r}=    GET Indicadores Custo    ${c}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    POST Fechamento Apuracao Custo    ${id}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    FECHADA
    ${r}=    POST Fechamento Apuracao Custo    ${id}
    Should Be Equal As Integers    ${r.status_code}    422
    ${r}=    POST Apuracao Custo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${r}=    GET Apuracao Custo    ${c}
    Should Be Equal As Integers    ${r.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Custos - By Cenario
    [Documentation]    Executes the cost scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'CENTROS'
        Custos - Centros - Status Code 201 409 404 E 422
    ELSE IF    '${caso}' == 'APURACAO'
        Custos - Apuracao - Status Code 422 400 200 E 404
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Custos.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
