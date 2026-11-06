*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/comunicacao/comunicacao_pom.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Canal
    [Documentation]    Returns the uuid of a channel by type.
    [Arguments]    ${tipo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Canais De Comunicacao
    ${ids}=    Evaluate    [c["uuid"] for c in $response.json() if c["tipo"] == $tipo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Do Comunicado
    [Documentation]    Builds a notice valid for the next days.
    [Arguments]    ${titulo}    ${publico}=POPULACAO    ${tipoCanal}=PORTAL    ${prioridade}=NORMAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${fim}=    Add Time To Date    ${hoje}    7 days    result_format=%Y-%m-%d
    ${canal}=    Id Do Canal    ${tipoCanal}
    ${canais}=    Create List    ${canal}
    ${payload}=    Create Dictionary    titulo=${titulo}    mensagem=Aviso de teste    prioridade=${prioridade}    publico=${publico}    inicio=${hoje}    fim=${fim}    canalIds=${canais}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Conteudo Aprovado
    [Documentation]    Creates a content and takes it to approved; returns its uuid and title.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${titulo}=    Set Variable    Robot Dengue ${sufixo}
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${validade}=    Add Time To Date    ${hoje}    90 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    titulo=${titulo}    resumo=Resumo    texto=Texto    tema=Dengue    publico=POPULACAO    validade=${validade}
    ${criado}=    POST Create Conteudo    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${uuid}=    Set Variable    ${criado.json()}[uuid]
    ${envio}=    POST Acao Do Conteudo    ${uuid}    envio-revisao
    Should Be Equal As Integers    ${envio.status_code}    200
    ${aprovado}=    POST Acao Do Conteudo    ${uuid}    aprovacao
    Should Be Equal As Integers    ${aprovado.status_code}    200
    RETURN    ${uuid}    ${titulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Comunicado - Status Code 201 E 200
    [Documentation]    A published notice goes to the portal; after it ends, it leaves.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${titulo}=    Set Variable    Robot Aviso ${sufixo}
    ${payload}=    Payload Do Comunicado    ${titulo}
    ${criado}=    POST Create Comunicado    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    Validate Object Schema    ${criado.json()}    ${COMUNICADO_SCHEMA}
    ${publicado}=    POST Publicar Comunicado    ${criado.json()}[uuid]
    Should Be Equal As Integers    ${publicado.status_code}    200
    Should Be Equal    ${publicado.json()}[publicacoes][0][situacao]    PUBLICADA
    ${avisos}=    GET Avisos Publicos
    ${titulos}=    Evaluate    [a["titulo"] for a in $avisos.json()]
    Should Contain    ${titulos}    ${titulo}
    ${motivo}=    Create Dictionary    motivo=Fim do aviso
    ${fim}=    POST Encerrar Comunicado    ${criado.json()}[uuid]    ${motivo}
    Should Be Equal As Integers    ${fim.status_code}    200
    ${depois}=    GET Avisos Publicos
    ${restantes}=    Evaluate    [a["titulo"] for a in $depois.json()]
    Should Not Contain    ${restantes}    ${titulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Canal Do Publico - Status Code 422
    [Documentation]    The population is reached only by the portal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Payload Do Comunicado    Robot Errado    POPULACAO    EMAIL
    ${response}=    POST Create Comunicado    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Sem Smtp - Status Code 200
    [Documentation]    Without SMTP the publication does not fail and records the result.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Payload Do Comunicado    Robot Interno ${sufixo}    PROFISSIONAIS    EMAIL
    ${criado}=    POST Create Comunicado    ${payload}
    ${publicado}=    POST Publicar Comunicado    ${criado.json()}[uuid]
    Should Be Equal As Integers    ${publicado.status_code}    200
    Should Be Equal    ${publicado.json()}[publicacoes][0][situacao]    NAO_ENVIADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Conteudo - Status Code 200
    [Documentation]    The content goes through review, reaches the portal and gets a new version.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${titulo}=    Seed Conteudo Aprovado
    ${portal}=    Id Do Canal    PORTAL
    ${canais}=    Create List    ${portal}
    ${payload}=    Create Dictionary    canalIds=${canais}
    ${publicado}=    POST Publicar Conteudo    ${uuid}    ${payload}
    Should Be Equal As Integers    ${publicado.status_code}    200
    Validate Object Schema    ${publicado.json()}    ${CONTEUDO_EDUCATIVO_SCHEMA}
    ${orientacoes}=    GET Orientacoes Publicas
    ${titulos}=    Evaluate    [o["titulo"] for o in $orientacoes.json()]
    Should Contain    ${titulos}    ${titulo}
    ${versao}=    POST Acao Do Conteudo    ${uuid}    nova-versao
    Should Be Equal As Integers    ${versao.status_code}    200
    Should Be Equal As Integers    ${versao.json()}[versao]    2
    ${repetida}=    POST Acao Do Conteudo    ${uuid}    nova-versao
    Should Be Equal As Integers    ${repetida.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Conteudo Sem Revisao - Status Code 422
    [Documentation]    A draft does not reach the portal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${titulo}=    Seed Conteudo Aprovado
    ${portal}=    Id Do Canal    PORTAL
    ${canais}=    Create List    ${portal}
    ${payload}=    Create Dictionary    canalIds=${canais}
    ${repetido}=    POST Acao Do Conteudo    ${uuid}    envio-revisao
    Should Be Equal As Integers    ${repetido.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Inexistente - Status Code 404
    [Documentation]    An unknown notice is not found.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Evaluate    str(__import__("uuid").uuid4())
    ${response}=    POST Publicar Comunicado    ${id}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-comunicado'    Comunicacao - Comunicado - Status Code 201 E 200
    Run Keyword If    '${meuHTTP}' == '422-canal'    Comunicacao - Canal Do Publico - Status Code 422
    Run Keyword If    '${meuHTTP}' == '200-smtp'    Comunicacao - Sem Smtp - Status Code 200
    Run Keyword If    '${meuHTTP}' == '200-conteudo'    Comunicacao - Conteudo - Status Code 200
    Run Keyword If    '${meuHTTP}' == '422-conteudo'    Comunicacao - Conteudo Sem Revisao - Status Code 422
    Run Keyword If    '${meuHTTP}' == '404-comunicado'    Comunicacao - Inexistente - Status Code 404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

