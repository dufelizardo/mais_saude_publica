*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/territorio/territorio_pom.resource
Resource    ../equipes/equipe/equipe_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Quadrado GeoJSON
    [Documentation]    Returns a GeoJSON square (WGS84) from the corner, as text.
    [Arguments]    ${lon}    ${lat}    ${lado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${texto}=    Evaluate    json.dumps({"type": "Polygon", "coordinates": [[[${lon}, ${lat}], [${lon} + ${lado}, ${lat}], [${lon} + ${lado}, ${lat} + ${lado}], [${lon}, ${lat} + ${lado}], [${lon}, ${lat}]]]})    modules=json
    RETURN    ${texto}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Territorio APS
    [Documentation]    Creates a segment and an APS territory; returns the territory uuid and the suffix.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${segmento}=    Create Dictionary    codigo=ROB${sufixo}    nome=Robot Distrito ${sufixo}    tipo=DISTRITO_SANITARIO
    ${s}=    POST Segmento Territorial    ${segmento}
    Should Be Equal As Integers    ${s.status_code}    201
    ${territorio}=    Create Dictionary    segmentoId=${s.json()}[uuid]    nome=Robot APS ${sufixo}    finalidade=ATENCAO_PRIMARIA
    ${t}=    POST Territorio    ${territorio}
    Should Be Equal As Integers    ${t.status_code}    201
    RETURN    ${t.json()}[uuid]    ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Area Territorial
    [Documentation]    Creates a segment, an APS territory and an area 0.02° wide; returns the area uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${territorio}    ${sufixo}=    Seed Territorio APS
    ${geometria}=    Quadrado GeoJSON    -47.0    -22.0    0.02
    ${area}=    Create Dictionary    territorioId=${territorio}    codigo=01    nome=Área Robot ${sufixo}    geometria=${geometria}
    ${a}=    POST Area Territorial    ${area}
    Should Be Equal As Integers    ${a.status_code}    201
    RETURN    ${a.json()}[area][uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Microarea
    [Documentation]    Creates a micro-area and returns its uuid.
    [Arguments]    ${area}    ${codigo}    ${geometria}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    areaId=${area}    codigo=${codigo}    geometria=${geometria}
    ${response}=    POST Microarea    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${ids}=    Evaluate    [m["uuid"] for m in $response.json()["area"]["microareas"] if m["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Membro Da Equipe
    [Documentation]    Adds a posted professional to the team with the function; returns the membership uuid.
    [Arguments]    ${equipe}    ${unidadeId}    ${funcao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    funcao=${funcao}
    ${response}=    POST Membro Equipe    ${equipe}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${ids}=    Evaluate    [m["uuid"] for m in $response.json()["membros"] if m["matricula"] == $matricula]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Arvore - Status Code 200
    [Documentation]    The tree comes with the indicators and a valid schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${response}=    GET Arvore Territorial
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${ARVORE_TERRITORIAL_SCHEMA}
    ${detalhe}=    GET Area Territorial    ${area}
    Should Be Equal As Integers    ${detalhe.status_code}    200
    Validate Object Schema    ${detalhe.json()}    ${AREA_TERRITORIAL_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Cadastro - Status Code 201
    [Documentation]    A micro-area inside the area is created, with the warnings of the tree.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${geometria}=    Quadrado GeoJSON    -47.0    -22.0    0.01
    ${payload}=    Create Dictionary    areaId=${area}    codigo=07    populacaoReferencia=${800}    geometria=${geometria}
    ${response}=    POST Microarea    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Should Contain    ${response.json()}[area][microareas][0][avisos]    Sem ACS
    Should Contain    ${response.json()}[area][microareas][0][avisos]    População de referência acima de 750 pessoas (CNES)
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Segmento - Status Code 409
    [Documentation]    The same segment code again is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${payload}=    Create Dictionary    codigo=ROB${sufixo}    nome=Robot Distrito    tipo=OUTRO
    ${primeiro}=    POST Segmento Territorial    ${payload}
    Should Be Equal As Integers    ${primeiro.status_code}    201
    ${response}=    POST Segmento Territorial    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - GeoJSON - Status Code 400
    [Documentation]    An open ring is not a valid polygon.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${aberto}=    Evaluate    json.dumps({"type": "Polygon", "coordinates": [[[-47.0, -22.0], [-46.99, -22.0], [-46.99, -21.99], [-47.0, -21.99]]]})    modules=json
    ${payload}=    Create Dictionary    areaId=${area}    codigo=09    geometria=${aberto}
    ${response}=    POST Microarea    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Should Contain    ${response.json()}[message]    Geometria inválida
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Fora Da Area - Status Code 422
    [Documentation]    A micro-area that leaves the area outline is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${geometria}=    Quadrado GeoJSON    -46.985    -21.985    0.01
    ${payload}=    Create Dictionary    areaId=${area}    codigo=08    geometria=${geometria}
    ${response}=    POST Microarea    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Should Contain    ${response.json()}[message]    sai do contorno
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Sobreposicao - Status Code 422
    [Documentation]    Two micro-areas of the same area do not overlap.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${primeira}=    Quadrado GeoJSON    -47.0    -22.0    0.01
    Seed Microarea    ${area}    07    ${primeira}
    ${sobreposta}=    Quadrado GeoJSON    -46.995    -21.995    0.004
    ${payload}=    Create Dictionary    areaId=${area}    codigo=08    geometria=${sobreposta}
    ${response}=    POST Microarea    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Should Contain    ${response.json()}[message]    sobrepõe
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Equipe E ACS - Status Code 200
    [Documentation]    The team takes the area, the agent the micro-area, and Equipes reads from the territory.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${micro}=    Seed Microarea    ${area}    07
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${acs}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${resp}=    Create Dictionary    equipeId=${equipe}
    ${r}=    POST Responsabilidade Territorial    ${area}    ${resp}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[area][equipe][nome]    ${nome}
    ${atrib}=    Create Dictionary    membroId=${acs}
    ${response}=    POST Atribuicao Microarea    ${micro}    ${atrib}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[area][microareas][0][acs][membroId]    ${acs}
    ${daEquipe}=    GET Territorio Da Equipe    ${equipe}
    Should Be Equal    ${daEquipe.json()}[area][uuid]    ${area}
    ${e}=    GET Equipe By Id    ${equipe}
    Should Be Equal    ${e.json()}[resumo][microareas]    07
    Should Be Equal    ${e.json()}[resumo][microareasPeloTerritorio]    ${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - ACS Nao E ACS - Status Code 422
    [Documentation]    A doctor of the team is not the agent of a micro-area.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${micro}=    Seed Microarea    ${area}    07
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${medico}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    MEDICO
    ${resp}=    Create Dictionary    equipeId=${equipe}
    POST Responsabilidade Territorial    ${area}    ${resp}
    ${atrib}=    Create Dictionary    membroId=${medico}
    ${response}=    POST Atribuicao Microarea    ${micro}    ${atrib}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Troca Sem Motivo - Status Code 400
    [Documentation]    Swapping the agent of a micro-area needs a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${micro}=    Seed Microarea    ${area}    07
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${acs1}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${acs2}=    Seed Membro Da Equipe    ${equipe}    ${unidadeId}    ACS
    ${resp}=    Create Dictionary    equipeId=${equipe}
    POST Responsabilidade Territorial    ${area}    ${resp}
    ${primeiro}=    Create Dictionary    membroId=${acs1}
    POST Atribuicao Microarea    ${micro}    ${primeiro}
    ${segundo}=    Create Dictionary    membroId=${acs2}
    ${response}=    POST Atribuicao Microarea    ${micro}    ${segundo}
    Should Be Equal As Integers    ${response.status_code}    400
    ${comMotivo}=    Create Dictionary    membroId=${acs2}    motivo=Redivisão da área
    ${troca}=    POST Atribuicao Microarea    ${micro}    ${comMotivo}
    Should Be Equal As Integers    ${troca.status_code}    200
    Length Should Be    ${troca.json()}[atribuicoes]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Coordenadas - Status Code 200
    [Documentation]    The unit gets latitude and longitude; only one of them is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${so}=    Create Dictionary    latitude=${-22.9}
    ${meia}=    PUT Coordenadas Unidade    ${unidadeId}    ${so}
    Should Be Equal As Integers    ${meia.status_code}    400
    ${payload}=    Create Dictionary    latitude=${-22.9}    longitude=${-47.06}
    ${response}=    PUT Coordenadas Unidade    ${unidadeId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${lista}=    GET Unidades No Mapa
    ${lat}=    Evaluate    [u["latitude"] for u in $lista.json() if u["uuid"] == $unidadeId][0]
    Should Be Equal As Numbers    ${lat}    -22.9
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - Area - Status Code 404
    [Documentation]    Fetches an area that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Area Territorial    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Territorio - By HTTP Status Code
    [Documentation]    Executes the territory scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-arvore'    Territorio - Arvore - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-cadastro'    Territorio - Cadastro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-segmento'    Territorio - Segmento - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400-geojson'    Territorio - GeoJSON - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-fora'    Territorio - Fora Da Area - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-sobreposicao'    Territorio - Sobreposicao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-equipe-acs'    Territorio - Equipe E ACS - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-funcao'    Territorio - ACS Nao E ACS - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-troca'    Territorio - Troca Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-coordenadas'    Territorio - Coordenadas - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Territorio - Area - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Territorio test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
