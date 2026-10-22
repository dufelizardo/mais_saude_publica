*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/programas/programa/programa_pom.resource
Resource    ../../../pom/equipamentos/rede_unidades/rede_unidades_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Da Categoria
    [Documentation]    Returns the uuid of a program category by name.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Categorias Programa
    ${ids}=    Evaluate    [c["uuid"] for c in $response.json() if c["nome"] == $nome]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Programa
    [Documentation]    Creates a program with a random code in "Crônicos" and returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${categoria}=    Id Da Categoria    Crônicos
    ${nome}=    Set Variable    Robot Programa ${sufixo}
    ${condicoes}=    Create List    I10
    ${payload}=    Create Dictionary    codigo=ROBOT_${sufixo}    nome=${nome}    categoriaId=${categoria}    idadeMinima=${40}    condicoes=${condicoes}
    ${response}=    POST Create Programa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[resumo][uuid]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Unidade Para Programa
    [Documentation]    Creates a unit of the given type and returns its uuid and name.
    [Arguments]    ${tipo}=UBS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot ${tipo} Programa ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    tipo=${tipo}
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[resumo][uuid]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Catalogo - Status Code 200
    [Documentation]    The initial load brings the four categories and the thirteen programs.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${categorias}=    GET Categorias Programa
    Should Be Equal As Integers    ${categorias.status_code}    200
    Validate Array Schema    ${categorias.json()}    ${CATEGORIA_PROGRAMA_SCHEMA}
    ${response}=    GET Programas
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PROGRAMA_RESUMO_SCHEMA}
    ${codigos}=    Evaluate    [p["codigo"] for p in $response.json()]
    FOR    ${codigo}    IN    SAUDE_DA_FAMILIA    HIPERDIA    PRE_NATAL    SAUDE_DO_IDOSO    SAUDE_MENTAL    TABAGISMO
        Should Contain    ${codigos}    ${codigo}
    END
    ${idoso}=    Evaluate    [p for p in $response.json() if p["codigo"] == "SAUDE_DO_IDOSO"][0]
    Should Be Equal    ${idoso}[publico]    60 anos ou mais
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Cadastro - Status Code 201
    [Documentation]    Creates a program with target audience; the code comes back upper case.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Programa
    ${response}=    GET Programa    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PROGRAMA_SCHEMA}
    Should Be Equal    ${response.json()}[resumo][publico]    40 anos ou mais · condições I10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Condicao Invalida - Status Code 400
    [Documentation]    A condition outside the CID-10/CIAP-2 format is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${categoria}=    Id Da Categoria    Crônicos
    ${condicoes}=    Create List    hipertensao
    ${payload}=    Create Dictionary    codigo=ROBOT_INVALIDO    nome=Inválido    categoriaId=${categoria}    condicoes=${condicoes}
    ${response}=    POST Create Programa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Codigo Repetido - Status Code 409
    [Documentation]    A program code cannot repeat.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${categoria}=    Id Da Categoria    Crônicos
    ${payload}=    Create Dictionary    codigo=HIPERDIA    nome=Repetido    categoriaId=${categoria}
    ${response}=    POST Create Programa    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Participacao - Status Code 200 409 E Encerramento
    [Documentation]    A unit joins the program, cannot join twice and the participation ends with a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    ${response}=    POST Participacao Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal As Integers    ${response.json()}[resumo][unidades]    1
    ${repetida}=    POST Participacao Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${repetida.status_code}    409
    ${participacao}=    Set Variable    ${response.json()}[participacoes][0][uuid]
    ${motivo}=    Create Dictionary    motivo=Unidade passou a referenciar o programa a outra UBS
    ${encerrada}=    POST Encerrar Participacao Programa    ${programa}    ${participacao}    ${motivo}
    Should Be Equal As Integers    ${encerrada.status_code}    200
    Length Should Be    ${encerrada.json()}[participacoes]    0
    Length Should Be    ${encerrada.json()}[participacoesEncerradas]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Nivel De Gestao - Status Code 422
    [Documentation]    Management levels do not offer programs.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${estado}    ${nomeEstado}=    Seed Unidade Para Programa    ESTADUAL
    ${payload}=    Create Dictionary    unidadeId=${estado}
    ${response}=    POST Participacao Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Consulta Publica - Status Code 200
    [Documentation]    The public listing shows active programs and where they are offered, without codes.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    POST Participacao Programa    ${programa}    ${payload}
    ${response}=    GET Programas Publicos
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PROGRAMA_PUBLICO_SCHEMA}
    ${item}=    Evaluate    [p for p in $response.json() if p["nome"] == $nome][0]
    Should Contain    ${item}[unidades]    ${nomeUnidade}
    Dictionary Should Not Contain Key    ${item}    codigo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - Inexistente - Status Code 404
    [Documentation]    A program that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Programa    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Programa - By HTTP Status Code
    [Documentation]    Executes the program scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Programa - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201'    Programa - Cadastro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    Programa - Condicao Invalida - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '409'    Programa - Codigo Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '200-participacao'    Programa - Participacao - Status Code 200 409 E Encerramento
    ...    ELSE IF    '${meuHTTP}' == '422'    Programa - Nivel De Gestao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-publico'    Programa - Consulta Publica - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Programa - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Programa test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
