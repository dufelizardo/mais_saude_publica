*** Settings ***
Library     String
Library     DateTime
Resource    ../../../pom/equipes/equipe/equipe_pom.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../cargo/create_cargo/create_cargo_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Uma Equipe
    [Documentation]    Creates an eSF in a fresh unit and returns the team uuid, the name and the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot SF ${sufixo}
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    tipo=ESF    nome=${nome}    microareas=14, 17
    ${response}=    POST Create Equipe    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[resumo][uuid]    ${nome}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Profissional Lotado
    [Documentation]    Creates a professional with a current posting in the unit and returns the registration.
    [Arguments]    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${cargoId}=    Seed A Cargo Uuid
    ${inicio}=    Get Current Date    increment=-30 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    matriculaProfissional=${matricula}    unidadeId=${unidadeId}    cargoId=${cargoId}    jornadaSemanalHoras=${40}    dataInicio=${inicio}    motivo=Admissão
    ${response}=    POST Create Lotacao Para Equipe    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Criar - Status Code 201
    [Documentation]    Creates a team with a valid schema; the same name again is refused with 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${response}=    GET Equipe By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${EQUIPE_SCHEMA}
    Should Be Equal    ${response.json()}[resumo][completa]    ${False}
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    tipo=ESF    nome=${nome}
    ${repetida}=    POST Create Equipe    ${payload}
    Should Be Equal As Integers    ${repetida.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Criar - Status Code 400 - Ine Invalido
    [Documentation]    Creates a team with a 3-digit INE.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    tipo=ESF    nome=Robot SF Ine    ine=123
    ${response}=    POST Create Equipe    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Membro - Status Code 200
    [Documentation]    Adds a posted doctor; the composition still lacks the other functions.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    funcao=MEDICO
    ${response}=    POST Membro Equipe    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Length Should Be    ${response.json()}[membros]    1
    Length Should Be    ${response.json()}[resumo][faltando]    3
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Membro - Status Code 422 - Sem Lotacao
    [Documentation]    Adds a professional without a posting in the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    funcao=ENFERMEIRO
    ${response}=    POST Membro Equipe    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Saida - Status Code 200
    [Documentation]    Ends a membership with a reason; it moves to the former members.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    funcao=ACS    microarea=14
    ${incluido}=    POST Membro Equipe    ${uuid}    ${payload}
    Should Be Equal As Integers    ${incluido.status_code}    200
    ${membroId}=    Set Variable    ${incluido.json()}[membros][0][uuid]
    ${saida}=    Create Dictionary    motivo=Mudou de microárea
    ${response}=    POST Saida Membro Equipe    ${membroId}    ${saida}
    Should Be Equal As Integers    ${response.status_code}    200
    Length Should Be    ${response.json()}[membros]    0
    Should Be Equal    ${response.json()}[antigos][0][motivoSaida]    Mudou de microárea
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Listar - Status Code 200
    [Documentation]    Lists the teams of a unit with the indicators.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${response}=    GET List Equipes    ${params}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${EQUIPES_SCHEMA}
    Should Be Equal As Integers    ${response.json()}[equipes]    1
    Should Be Equal As Integers    ${response.json()}[incompletas]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - Detalhe - Status Code 404
    [Documentation]    Fetches a team that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Equipe By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Equipe - By HTTP Status Code
    [Documentation]    Executes the team scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Equipe - Criar - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-ine'    Equipe - Criar - Status Code 400 - Ine Invalido
    ...    ELSE IF    '${meuHTTP}' == '200-membro'    Equipe - Membro - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-lotacao'    Equipe - Membro - Status Code 422 - Sem Lotacao
    ...    ELSE IF    '${meuHTTP}' == '200-saida'    Equipe - Saida - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-lista'    Equipe - Listar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Equipe - Detalhe - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Equipe test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
