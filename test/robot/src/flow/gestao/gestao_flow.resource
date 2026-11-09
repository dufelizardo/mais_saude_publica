*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/gestao/gestao_pom.resource
Resource    ../regional/get_regional_by_nome/get_regional_by_nome_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Payload Da Area
    [Documentation]    Builds a management area.
    [Arguments]    ${tipo}    ${superior}=${EMPTY}    ${regional}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${payload}=    Create Dictionary    tipo=${tipo}    tema=ATENCAO_BASICA    nome=Robot ${tipo} ${sufixo}
    IF    '${superior}' != '${EMPTY}'
        Set To Dictionary    ${payload}    superiorId=${superior}
    END
    IF    '${regional}' != '${EMPTY}'
        Set To Dictionary    ${payload}    regionalId=${regional}
    END
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Uuid De Uma Regional Nova
    [Documentation]    Seeds a regional and returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nome}=    Seed A Regional Unit
    ${lista}=    GET Regionais Da Gestao
    ${uuid}=    Evaluate    [r["uuid"] for r in $lista.json() if r["nome"] == $nome][0]
    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Gestao - Coordenacao Regional - Status Code 201
    [Documentation]    A management with a regional coordination.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${gerencia}=    Payload Da Area    GERENCIA
    ${g}=    POST Area De Gestao    ${gerencia}
    Should Be Equal As Integers    ${g.status_code}    201
    Validate Object Schema    ${g.json()}    ${AREA_GESTAO_SCHEMA}
    ${regional}=    Uuid De Uma Regional Nova
    ${coordenacao}=    Payload Da Area    COORDENACAO    ${g.json()}[uuid]    ${regional}
    ${c}=    POST Area De Gestao    ${coordenacao}
    Should Be Equal As Integers    ${c.status_code}    201
    Should Be Equal    ${c.json()}[regionalId]    ${regional}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Gestao - Regional Repetida - Status Code 409
    [Documentation]    One active coordination per regional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${regional}=    Uuid De Uma Regional Nova
    ${primeira}=    Payload Da Area    COORDENACAO    ${EMPTY}    ${regional}
    ${p}=    POST Area De Gestao    ${primeira}
    Should Be Equal As Integers    ${p.status_code}    201
    ${segunda}=    Payload Da Area    COORDENACAO    ${EMPTY}    ${regional}
    ${response}=    POST Area De Gestao    ${segunda}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Gestao - Hierarquia - Status Code 422
    [Documentation]    A supervision needs a superior; a regional must be a regional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${supervisao}=    Payload Da Area    SUPERVISAO
    ${response}=    POST Area De Gestao    ${supervisao}
    Should Be Equal As Integers    ${response.status_code}    422
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${coordenacao}=    Payload Da Area    COORDENACAO    ${EMPTY}    ${ubs}
    ${outra}=    POST Area De Gestao    ${coordenacao}
    Should Be Equal As Integers    ${outra.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Gestao - Titular - Status Code 200
    [Documentation]    Designates the head and replaces it, keeping the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${gerencia}=    Payload Da Area    GERENCIA
    ${g}=    POST Area De Gestao    ${gerencia}
    ${primeiro}=    Seed A Profissional Matricula
    ${segundo}=    Seed A Profissional Matricula
    ${ontem}=    Get Current Date    increment=-10 days    result_format=%Y-%m-%d
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${d1}=    Create Dictionary    matriculaProfissional=${primeiro}    papel=TITULAR    inicio=${ontem}    ato=Portaria 1/2026
    ${r1}=    POST Designar Na Area    ${g.json()}[uuid]    ${d1}
    Should Be Equal As Integers    ${r1.status_code}    200
    ${d2}=    Create Dictionary    matriculaProfissional=${segundo}    papel=TITULAR    inicio=${hoje}
    ${r2}=    POST Designar Na Area    ${g.json()}[uuid]    ${d2}
    Should Be Equal As Integers    ${r2.status_code}    200
    Should Be Equal    ${r2.json()}[titular][matricula]    ${segundo}
    Length Should Be    ${r2.json()}[historico]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Gestao - Indicadores Da Regional - Status Code 200
    [Documentation]    The regional scope reads the indicators.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${regional}=    Uuid De Uma Regional Nova
    ${response}=    GET Vigilancia Na Unidade    ${regional}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Not Be Empty    ${response.json()}[abrangencia]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Gestao - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-coordenacao'    Gestao - Coordenacao Regional - Status Code 201
    Run Keyword If    '${meuHTTP}' == '409-regional'    Gestao - Regional Repetida - Status Code 409
    Run Keyword If    '${meuHTTP}' == '422-hierarquia'    Gestao - Hierarquia - Status Code 422
    Run Keyword If    '${meuHTTP}' == '200-titular'    Gestao - Titular - Status Code 200
    Run Keyword If    '${meuHTTP}' == '200-indicadores'    Gestao - Indicadores Da Regional - Status Code 200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

