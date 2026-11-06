*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/contingencia_pom.resource
Resource    emergencias_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Plano de contingência versionado e ativação na emergência (ADR-0157).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Corpo Do Plano
    [Documentation]    The plan body: the levels, one criterion and one action per level (2 h).
    [Arguments]    ${codigo}    ${tipo}    ${niveis}    ${comAcoes}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${criterios}=    Evaluate    [{"nivelId": n, "descricao": "Rio acima de 4 m"} for n in $niveis]
    ${acoes}=    Evaluate    ([{"nivelId": n, "descricao": "Abrir o COE", "responsavel": "Coordenação de vigilância", "prazoHoras": 2} for n in $niveis]) if $comAcoes else []
    ${payload}=    Create Dictionary    codigo=${codigo}    nome=Plano Robot    tipoId=${tipo}    objetivo=Responder à emergência    vigenciaInicio=${hoje}    nivelIds=${niveis}    criterios=${criterios}    acoes=${acoes}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Codigo De Plano Novo
    [Documentation]    A unique plan code.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    RETURN    ROBOT_${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Plano Vigente
    [Documentation]    A current plan of the type covering the levels; returns the uuid.
    [Arguments]    ${tipoCodigo}=INUNDACAO    @{ordens}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Plano Novo
    ${niveis}=    Create List
    FOR    ${o}    IN    @{ordens}
        ${n}=    Id Do Nivel Emergencia    ${o}
        Append To List    ${niveis}    ${n}
    END
    ${tipo}=    Id Do Tipo Emergencia    ${tipoCodigo}
    ${payload}=    Corpo Do Plano    ${codigo}    ${tipo}    ${niveis}
    ${r}=    POST Plano Contingencia    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${p}=    POST Publicar Plano    ${r.json()}[uuid]
    Should Be Equal As Integers    ${p.status_code}    200
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ativar Plano Na Emergencia
    [Documentation]    Activates the plan; returns the response.
    [Arguments]    ${emergencia}    ${plano}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    planoId=${plano}    autoridade=Secretário de Saúde    justificativa=Cota de alerta atingida
    ${r}=    POST Ativar Plano    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Plano - Status Code 201
    [Documentation]    The plan is born as draft, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Plano Novo
    ${tipo}=    Id Do Tipo Emergencia
    ${n1}=    Id Do Nivel Emergencia    1
    ${niveis}=    Create List    ${n1}
    ${payload}=    Corpo Do Plano    ${codigo}    ${tipo}    ${niveis}
    ${r}=    POST Plano Contingencia    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${PLANO_CONTINGENCIA_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    RASCUNHO
    Should Be Equal As Integers    ${r.json()}[versao]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Codigo Repetido - Status Code 409
    [Documentation]    The code is unique.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Plano Novo
    ${tipo}=    Id Do Tipo Emergencia
    ${n1}=    Id Do Nivel Emergencia    1
    ${niveis}=    Create List    ${n1}
    ${payload}=    Corpo Do Plano    ${codigo}    ${tipo}    ${niveis}
    POST Plano Contingencia    ${payload}
    ${r}=    POST Plano Contingencia    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Publicar Sem Acao - Status Code 422
    [Documentation]    Publishing needs at least one action.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${codigo}=    Codigo De Plano Novo
    ${tipo}=    Id Do Tipo Emergencia
    ${n1}=    Id Do Nivel Emergencia    1
    ${niveis}=    Create List    ${n1}
    ${payload}=    Corpo Do Plano    ${codigo}    ${tipo}    ${niveis}    ${False}
    ${c}=    POST Plano Contingencia    ${payload}
    ${r}=    POST Publicar Plano    ${c.json()}[uuid]
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Publicar - Status Code 200
    [Documentation]    The draft becomes current.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Plano Vigente    INUNDACAO    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Revisao - Status Code 201
    [Documentation]    Revising opens version 2 as draft, with the actions copied.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Plano Vigente    INUNDACAO    1
    ${r}=    POST Revisar Plano    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[versao]    2
    Should Be Equal    ${r.json()}[situacao]    RASCUNHO
    Length Should Be    ${r.json()}[acoes]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Editar Vigente - Status Code 422
    [Documentation]    Only the draft is edited.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Plano Vigente    INUNDACAO    1
    ${tipo}=    Id Do Tipo Emergencia
    ${n1}=    Id Do Nivel Emergencia    1
    ${niveis}=    Create List    ${n1}
    ${payload}=    Corpo Do Plano    ${None}    ${tipo}    ${niveis}
    ${r}=    PUT Plano Contingencia    ${uuid}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Ativacao - Status Code 201
    [Documentation]    The plan is active in the emergency, with the actions of the level.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    ${r}=    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[planos][0][situacao]    ATIVA
    Should Be Equal    ${r.json()}[planos][0][acoesDoNivel][0][descricao]    Abrir o COE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Ja Ativo - Status Code 409
    [Documentation]    The same plan is active only once.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    ${r}=    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Tipo Diferente - Status Code 422
    [Documentation]    An epidemic plan does not apply to a flood.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    EPIDEMIA    1
    ${r}=    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Nivel Alterado - Status Code 200
    [Documentation]    The level change enters the history of the active plan.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1    2
    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    ${n2}=    Id Do Nivel Emergencia    2
    ${payload}=    Create Dictionary    nivelId=${n2}    justificativa=Hospital atingido
    ${r}=    POST Nivel Emergencia    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[planos][0][registros][0][tipo]    NIVEL_ALTERADO
    Should Be True    ${r.json()}[planos][0][cobreNivelAtual]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - Desativacao - Status Code 200
    [Documentation]    Deactivating with the justification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    ${a}=    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    ${payload}=    Create Dictionary    justificativa=Situação controlada
    ${r}=    POST Desativar Plano    ${em.json()}[uuid]    ${a.json()}[planos][0][ativacaoId]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[planos][0][situacao]    DESATIVADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contingencia - By HTTP Status Code
    [Documentation]    Executes the E2 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-plano'    Contingencia - Plano - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-codigo-repetido'    Contingencia - Codigo Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-publicar-sem-acao'    Contingencia - Publicar Sem Acao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-publicar'    Contingencia - Publicar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-revisao'    Contingencia - Revisao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-editar-vigente'    Contingencia - Editar Vigente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-ativacao'    Contingencia - Ativacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-ja-ativo'    Contingencia - Ja Ativo - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-tipo-diferente'    Contingencia - Tipo Diferente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-nivel-alterado'    Contingencia - Nivel Alterado - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-desativacao'    Contingencia - Desativacao - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Contingencia test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
