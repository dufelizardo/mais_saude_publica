*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/equipamentos/rede_unidades/rede_unidades_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Uma Unidade Por Id
    [Documentation]    Creates a UBS with a random name (and optional CNES) and returns its uuid and name.
    [Arguments]    ${cnes}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot UBS Rede ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    tipo=UBS
    IF    "${cnes}" != "${EMPTY}"    Set To Dictionary    ${payload}    cnes=${cnes}
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[resumo][uuid]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Listar - Status Code 200
    [Documentation]    Lists the network with valid schemas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    ${response}=    GET Rede Unidades
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${REDE_UNIDADES_SCHEMA}
    Validate Array Schema    ${response.json()}[rede]    ${REDE_UNIDADE_RESUMO_SCHEMA}
    ${nomes}=    Evaluate    [u["nome"] for u in $response.json()["rede"]]
    Should Contain    ${nomes}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Criar - Status Code 409 - Nome Repetido
    [Documentation]    Creates a unit with an existing name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    ${payload}=    Create Dictionary    nome=${nome}    tipo=UBS
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Criar - Status Code 400 - Cnes Invalido
    [Documentation]    Creates a unit with a 5-digit CNES.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Create Dictionary    nome=Robot UBS Cnes ${sufixo}    tipo=UBS    cnes=12345
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Editar - Status Code 200
    [Documentation]    Edits the name and the CNES; the file comes back updated.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    ${cnes}=    Generate Random String    7    [NUMBERS]
    ${payload}=    Create Dictionary    nome=${nome} Editada    tipo=UBS    cnes=${cnes}
    ${response}=    PATCH Update Unidade Por Id    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${FICHA_UNIDADE_SCHEMA}
    Should Be Equal    ${response.json()}[resumo][cnes]    ${cnes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Horarios - Status Code 200 E 400
    [Documentation]    Sets Monday 08-12; overlapping turns are refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    ${t1}=    Create Dictionary    diaSemana=MONDAY    abre=08:00    fecha=12:00
    ${t2}=    Create Dictionary    diaSemana=MONDAY    abre=11:00    fecha=17:00
    ${sobrepostos}=    Create List    ${t1}    ${t2}
    ${payload}=    Create Dictionary    funciona24h=${False}    turnos=${sobrepostos}
    ${recusado}=    PUT Horarios Unidade    ${uuid}    ${payload}
    Should Be Equal As Integers    ${recusado.status_code}    400
    ${turnos}=    Create List    ${t1}
    ${payload}=    Create Dictionary    funciona24h=${False}    turnos=${turnos}
    ${response}=    PUT Horarios Unidade    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Length Should Be    ${response.json()}[turnos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Situacao - Status Code 200 E 400
    [Documentation]    Puts the unit under construction with a reason; without reason, 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Uma Unidade Por Id
    ${sem}=    Create Dictionary    situacao=EM_OBRA
    ${recusado}=    POST Situacao Unidade    ${uuid}    ${sem}
    Should Be Equal As Integers    ${recusado.status_code}    400
    ${payload}=    Create Dictionary    situacao=EM_OBRA    motivo=Reforma do telhado
    ${response}=    POST Situacao Unidade    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[resumo][situacaoOperacional]    EM_OBRA
    Length Should Be    ${response.json()}[historico]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Ficha - Status Code 404
    [Documentation]    Fetches a unit that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Ficha Unidade    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede Unidades - By HTTP Status Code
    [Documentation]    Executes the network scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-rede'    Rede - Listar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '409'    Rede - Criar - Status Code 409 - Nome Repetido
    ...    ELSE IF    '${meuHTTP}' == '400-cnes'    Rede - Criar - Status Code 400 - Cnes Invalido
    ...    ELSE IF    '${meuHTTP}' == '200-editar'    Rede - Editar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-horarios'    Rede - Horarios - Status Code 200 E 400
    ...    ELSE IF    '${meuHTTP}' == '200-situacao'    Rede - Situacao - Status Code 200 E 400
    ...    ELSE IF    '${meuHTTP}' == '404'    Rede - Ficha - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Rede Unidades test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
