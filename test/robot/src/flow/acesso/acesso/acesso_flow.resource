*** Settings ***
Library    Collections
Resource    ../../../pom/acesso/acesso/acesso_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Papéis e escopo por unidade (ADR-0066). A suíte não tem credencial de login nem cria usuários: os casos de
    concessão usam um usuário já existente e um papel próprio sem permissões, revogado ao fim (Skip se não houver usuário).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Codigo De Papel Unico
    [Documentation]    Returns a unique Papel code for this run.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Evaluate    uuid.uuid4().hex[:10].upper()    modules=uuid
    RETURN    ROBOT_${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Papel Sem Permissoes
    [Documentation]    Creates a custom Papel without permissions and returns its uuid and code.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Papel Unico
    ${permissoes}=    Create List
    ${payload}=    Create Dictionary    codigo=${codigo}    nome=Papel Robot ${codigo}    permissoes=${permissoes}
    ${criado}=    POST Create Papel    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${uuid}=    Buscar Papel Uuid Por Codigo    ${codigo}
    RETURN    ${uuid}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Papel Uuid Por Codigo
    [Documentation]    Lists the Papéis and returns the uuid of ${codigo}.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET List Papeis
    Should Be Equal As Integers    ${lista.status_code}    200
    ${uuid}=    Evaluate    [p['uuid'] for p in $lista.json() if p['codigo'] == $codigo][0]
    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET List Permissoes - Status Code 200
    [Documentation]    Lists the permission catalog, expecting HTTP 200 and the seeded codes.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET List Permissoes
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PERMISSAO_SCHEMA}
    ${codigos}=    Evaluate    [p['codigo'] for p in $response.json()]
    List Should Contain Value    ${codigos}    ACESSO.GERENCIAR
    List Should Contain Value    ${codigos}    TRIAGEM.REGISTRAR
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Permissoes - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate List Permissoes test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET List Permissoes - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for List Permissoes test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET List Papeis - Status Code 200
    [Documentation]    Lists the Papéis, expecting HTTP 200 and the técnico de enfermagem without triagem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET List Papeis
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PAPEL_SCHEMA}
    ${tecnico}=    Evaluate    [p for p in $response.json() if p['codigo'] == 'TECNICO_DE_ENFERMAGEM'][0]
    List Should Contain Value    ${tecnico}[permissoes]    MEDICACAO.ADMINISTRAR
    List Should Not Contain Value    ${tecnico}[permissoes]    TRIAGEM.REGISTRAR
    List Should Not Contain Value    ${tecnico}[permissoes]    EVOLUCAO.REGISTRAR
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Papel By Id - Status Code 200
    [Documentation]    Fetches the platform administrator Papel, expecting HTTP 200 and no health-data permission.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Buscar Papel Uuid Por Codigo    ADMINISTRADOR_PLATAFORMA
    ${response}=    GET Get Papel By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PAPEL_SCHEMA}
    Should Be True    ${response.json()}[padrao]
    List Should Not Contain Value    ${response.json()}[permissoes]    PRONTUARIO.CONSULTAR
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Papel By Id - Status Code 404
    [Documentation]    Fetches a Papel by a random uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Papel By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Papeis - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate List Papeis test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET List Papeis - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for List Papeis test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Papel By Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Papel By Id test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Papel By Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Papel By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Papel By Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Papel - Status Code 201
    [Documentation]    Creates a custom Papel, expecting HTTP 201 and the Papel listed as non-default.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Papel Unico
    ${permissoes}=    Create List    PACIENTE.CONSULTAR    agendamento.gerenciar
    ${payload}=    Create Dictionary    codigo=${codigo}    nome=Agente Robot    permissoes=${permissoes}
    ${response}=    POST Create Papel    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}
    ${uuid}=    Buscar Papel Uuid Por Codigo    ${codigo}
    ${papel}=    GET Get Papel By Id    ${uuid}
    Should Not Be True    ${papel.json()}[padrao]
    List Should Contain Value    ${papel.json()}[permissoes]    AGENDAMENTO.GERENCIAR
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Papel - Status Code 409
    [Documentation]    Creates a Papel with a seeded code, expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${permissoes}=    Create List
    ${payload}=    Create Dictionary    codigo=ENFERMEIRO    nome=Outro    permissoes=${permissoes}
    ${response}=    POST Create Papel    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Papel - Status Code 400
    [Documentation]    Creates a Papel with a permission outside the catalog, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Papel Unico
    ${permissoes}=    Create List    NAO.EXISTE
    ${payload}=    Create Dictionary    codigo=${codigo}    nome=Invalido    permissoes=${permissoes}
    ${response}=    POST Create Papel    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Papel - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Papel test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Papel - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Papel - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Papel - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Papel test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Papel - Status Code 200
    [Documentation]    Deactivates a custom Papel, expecting HTTP 200 and the change persisted.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${codigo}=    Seed Um Papel Sem Permissoes
    ${permissoes}=    Create List    PACIENTE.CONSULTAR
    ${payload}=    Create Dictionary    nome=Papel Robot Atualizado    ativo=${False}    permissoes=${permissoes}
    ${response}=    PATCH Update Papel    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}
    ${papel}=    GET Get Papel By Id    ${uuid}
    Should Be Equal    ${papel.json()}[codigo]    ${codigo}
    Should Not Be True    ${papel.json()}[ativo]
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Papel - Status Code 422
    [Documentation]    Tries to deactivate the platform administrator Papel, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Buscar Papel Uuid Por Codigo    ADMINISTRADOR_PLATAFORMA
    ${permissoes}=    Create List    USUARIO.GERENCIAR    ACESSO.GERENCIAR    ORGANIZACAO.GERENCIAR
    ${payload}=    Create Dictionary    nome=Administrador da plataforma    ativo=${False}    permissoes=${permissoes}
    ${response}=    PATCH Update Papel    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Papel - Status Code 404
    [Documentation]    Updates a Papel by a random uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${permissoes}=    Create List
    ${payload}=    Create Dictionary    nome=Qualquer    ativo=${True}    permissoes=${permissoes}
    ${response}=    PATCH Update Papel    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Papel - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Papel test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Papel - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422'    PATCH Update Papel - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Papel - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Papel test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET List Atribuicoes Acesso - Status Code 200
    [Documentation]    Lists every AtribuicaoAcesso, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET List Atribuicoes Acesso
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${ATRIBUICAO_ACESSO_SCHEMA}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Atribuicoes - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate List Atribuicoes Acesso test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET List Atribuicoes Acesso - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for List Atribuicoes Acesso test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atribuicao Acesso - Status Code 201
    [Documentation]    Grants a permission-less custom Papel to an existing Usuario (network-wide), expecting HTTP 201; then revokes it (HTTP 200) and revokes again (HTTP 422). Skipped when there is no Usuario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${usuarios}=    GET List Usuarios
    Should Be Equal As Integers    ${usuarios.status_code}    200
    ${quantidade}=    Get Length    ${usuarios.json()}
    Skip If    ${quantidade} == 0    Nenhum usuário cadastrado neste ambiente para receber a atribuição.
    ${usuarioId}=    Set Variable    ${usuarios.json()}[0][uuid]
    ${papelId}    ${codigo}=    Seed Um Papel Sem Permissoes

    ${payload}=    Create Dictionary    usuarioId=${usuarioId}    papelId=${papelId}
    ${response}=    POST Create Atribuicao Acesso    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${repetida}=    POST Create Atribuicao Acesso    ${payload}
    Should Be Equal As Integers    ${repetida.status_code}    409

    ${lista}=    GET List Atribuicoes Acesso    ?usuarioId=${usuarioId}
    ${atribuicaoId}=    Evaluate    [a['uuid'] for a in $lista.json() if a['papelCodigo'] == $codigo][0]
    ${motivo}=    Create Dictionary    motivo=Limpeza da suíte Robot
    ${revogada}=    POST Revogar Atribuicao Acesso    ${atribuicaoId}    ${motivo}
    Should Be Equal As Integers    ${revogada.status_code}    200
    ${outra}=    POST Revogar Atribuicao Acesso    ${atribuicaoId}    ${motivo}
    Should Be Equal As Integers    ${outra.status_code}    422
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atribuicao Acesso - Status Code 404
    [Documentation]    Grants a Papel to a random, non-existent Usuario, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${papelId}=    Buscar Papel Uuid Por Codigo    MEDICO
    ${payload}=    Create Dictionary    usuarioId=${uuidInexistente}    papelId=${papelId}
    ${response}=    POST Create Atribuicao Acesso    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Atribuicao Acesso - Status Code 400
    [Documentation]    Grants without usuarioId and papelId, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ${response}=    POST Create Atribuicao Acesso    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Atribuicao - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Atribuicao Acesso test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Atribuicao Acesso - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Create Atribuicao Acesso - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Atribuicao Acesso - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Atribuicao Acesso test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Revogar Atribuicao Acesso - Status Code 404
    [Documentation]    Revokes a random, non-existent AtribuicaoAcesso, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${motivo}=    Create Dictionary    motivo=Qualquer
    ${response}=    POST Revogar Atribuicao Acesso    ${uuidInexistente}    ${motivo}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Revogar Atribuicao Acesso - Status Code 400
    [Documentation]    Revokes without a reason, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${motivo}=    Create Dictionary    motivo=${EMPTY}
    ${response}=    POST Revogar Atribuicao Acesso    ${uuidInexistente}    ${motivo}
    Should Be Equal As Integers    ${response.status_code}    400
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Revogacao - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Revogar Atribuicao Acesso test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '404'    POST Revogar Atribuicao Acesso - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Revogar Atribuicao Acesso - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Revogar Atribuicao Acesso test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET List Usuarios - Status Code 200
    [Documentation]    Lists every Usuario, expecting HTTP 200 and no password hash.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET List Usuarios
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${USUARIO_SCHEMA}
    Should Not Contain    ${response.text}    senhaHash
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Usuario By Id - Status Code 404
    [Documentation]    Fetches a Usuario by a random uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Usuario By Id    ${uuidInexistente}
    Should Be Equal As Integers    ${response.status_code}    404
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acesso - Usuarios - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Usuarios test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET List Usuarios - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Usuario By Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Usuarios test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

