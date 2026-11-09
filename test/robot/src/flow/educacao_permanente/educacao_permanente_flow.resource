*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/educacao_permanente/educacao_permanente_pom.resource
Resource    ../treinamento/update_treinamento/update_treinamento_flow.resource
Resource    ../comunicacao/comunicacao_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Da Competencia
    [Documentation]    Returns the uuid of a competency by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Competencias
    ${uuid}=    Evaluate    [c["uuid"] for c in $lista.json() if c["codigo"] == $codigo][0]
    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Grupo Do Conteudo
    [Documentation]    Returns the version group of a content.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Conteudos Educativos
    ${grupo}=    Evaluate    [c["grupoId"] for c in $lista.json() if c["uuid"] == $uuid][0]
    RETURN    ${grupo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao Permanente - Catalogo - Status Code 200 E 409
    [Documentation]    The initial load and the repeated code.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Competencias
    Should Be Equal As Integers    ${response.status_code}    200
    ${codigos}=    Evaluate    [c["codigo"] for c in $response.json()]
    Should Contain    ${codigos}    BIOSSEGURANCA
    ${repetida}=    Create Dictionary    codigo=BIOSSEGURANCA    nome=Repetida
    ${conflito}=    POST Competencia    ${repetida}
    Should Be Equal As Integers    ${conflito.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao Permanente - Ligar - Status Code 200
    [Documentation]    Links a competency and a reviewed content to a training.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${treinamento}    ${nome}=    Seed A Treinamento
    ${competencia}=    Id Da Competencia    BIOSSEGURANCA
    ${conteudo}    ${titulo}=    Seed Conteudo Aprovado
    ${grupo}=    Grupo Do Conteudo    ${conteudo}
    ${competencias}=    Create List    ${competencia}
    ${conteudos}=    Create List    ${grupo}
    ${payload}=    Create Dictionary    competenciaIds=${competencias}    conteudoGrupoIds=${conteudos}
    ${response}=    PUT Educacao Do Treinamento    ${treinamento}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${EDUCACAO_DO_TREINAMENTO_SCHEMA}
    Should Be Equal    ${response.json()}[competencias][0][codigo]    BIOSSEGURANCA
    Should Be Equal    ${response.json()}[conteudos][0][titulo]    ${titulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao Permanente - Conteudo Sem Revisao - Status Code 422
    [Documentation]    A draft content does not enter a training.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${treinamento}    ${nome}=    Seed A Treinamento
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${validade}=    Add Time To Date    ${hoje}    90 days    result_format=%Y-%m-%d
    ${conteudo}=    Create Dictionary    titulo=Robot Rascunho    resumo=Resumo    texto=Texto    tema=Tema    publico=PROFISSIONAIS    validade=${validade}
    ${criado}=    POST Create Conteudo    ${conteudo}
    ${competencias}=    Create List
    ${conteudos}=    Create List    ${criado.json()}[grupoId]
    ${payload}=    Create Dictionary    competenciaIds=${competencias}    conteudoGrupoIds=${conteudos}
    ${response}=    PUT Educacao Do Treinamento    ${treinamento}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao Permanente - Profissional - Status Code 200
    [Documentation]    The competency comes from the participation.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${treinamento}    ${nome}=    Seed A Treinamento
    ${competencia}=    Id Da Competencia    SUPORTE_BASICO_VIDA
    ${competencias}=    Create List    ${competencia}
    ${conteudos}=    Create List
    ${payload}=    Create Dictionary    competenciaIds=${competencias}    conteudoGrupoIds=${conteudos}
    ${ligado}=    PUT Educacao Do Treinamento    ${treinamento}    ${payload}
    Should Be Equal As Integers    ${ligado.status_code}    200
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${participacao}=    Create Dictionary    matriculaProfissional=${matricula}    treinamentoId=${treinamento}    dataConclusao=${hoje}
    ${registrada}=    POST Participacao Treinamento    ${participacao}
    Should Be True    ${registrada.status_code} < 300
    ${response}=    GET Competencias Do Profissional    ${matricula}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[0][codigo]    SUPORTE_BASICO_VIDA
    Should Not Be True    ${response.json()}[0][vencida]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao Permanente - Profissional Inexistente - Status Code 404
    [Documentation]    An unknown professional is not found.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Competencias Do Profissional    NAO-EXISTE-C4
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao Permanente - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-409-catalogo'    Educacao Permanente - Catalogo - Status Code 200 E 409
    Run Keyword If    '${meuHTTP}' == '200-ligar'    Educacao Permanente - Ligar - Status Code 200
    Run Keyword If    '${meuHTTP}' == '422-rascunho'    Educacao Permanente - Conteudo Sem Revisao - Status Code 422
    Run Keyword If    '${meuHTTP}' == '200-profissional'    Educacao Permanente - Profissional - Status Code 200
    Run Keyword If    '${meuHTTP}' == '404-profissional'    Educacao Permanente - Profissional Inexistente - Status Code 404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

