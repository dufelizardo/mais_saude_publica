*** Settings ***
Library     Collections
Library     DateTime
Resource    ./saude_mental_flow.resource
Resource    ../internacao/leito_internacao/leito_internacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Mental, fatia SM3 (ADR-0123): risco com histórico, crise e internação psiquiátrica.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Saude Mental - Risco - Status Code 201
    [Documentation]    High risk with measures and reassessment; the case flags it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${reavaliar}=    Get Current Date    increment=7 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=AUTOAGRESSAO    nivel=ALTO    medidas=Plano de segurança e contato diário.    reavaliarEm=${reavaliar}
    ${response}=    POST Risco Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Should Be True    ${response.json()}[riscoAlto]
    Validate Object Schema    ${response.json()}[riscos][0]    ${AVALIACAO_RISCO_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Risco Sem Medidas - Status Code 400
    [Documentation]    Moderate or high risk needs the measures.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    tipo=AUTOAGRESSAO    nivel=MODERADO
    ${response}=    POST Risco Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Crise - Status Code 201
    [Documentation]    A crisis linked to the case, with the notification suggested.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    tipo=TENTATIVA_DE_SUICIDIO    descricao=Ingestão de medicamentos.
    ${r}=    POST Crise Saude Mental    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${crise}=    Set Variable    ${r.json()}[uuid]
    Validate Object Schema    ${r.json()}    ${EVENTO_CRISE_SCHEMA}
    Should Be Equal    ${r.json()}[acompanhamentoId]    ${caso}
    Should Be True    ${r.json()}[notificacaoPendente]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Encerrar Sem Pos Crise - Status Code 422
    [Documentation]    Closing needs the post-crisis step; with it, closes.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    tipo=TENTATIVA_DE_SUICIDIO    descricao=Ingestão de medicamentos.
    ${r}=    POST Crise Saude Mental    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${crise}=    Set Variable    ${r.json()}[uuid]
    ${desfecho}=    Create Dictionary    desfecho=RETORNO_AO_DOMICILIO
    ${r422}=    POST Encerrar Crise Saude Mental    ${crise}    ${desfecho}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${etapa}=    Create Dictionary    etapa=POS_CRISE    descricao=Retorno ao CAPS em 24 horas.
    ${re}=    POST Etapa Crise Saude Mental    ${crise}    ${etapa}
    Should Be Equal As Integers    ${re.status_code}    201
    ${r200}=    POST Encerrar Crise Saude Mental    ${crise}    ${desfecho}
    Should Be Equal As Integers    ${r200.status_code}    200
    Should Be Equal    ${r200.json()}[situacao]    ENCERRADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Notificacao - Status Code 200
    [Documentation]    Registers the notification once; a second time is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    tipo=TENTATIVA_DE_SUICIDIO    descricao=Ingestão de medicamentos.
    ${r}=    POST Crise Saude Mental    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${crise}=    Set Variable    ${r.json()}[uuid]
    ${payload}=    Create Dictionary    numero=SINAN-123
    ${r1}=    POST Notificacao Crise Saude Mental    ${crise}    ${payload}
    Should Be Equal As Integers    ${r1.status_code}    200
    Should Not Be True    ${r1.json()}[notificacaoPendente]
    ${r2}=    POST Notificacao Crise Saude Mental    ${crise}    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Crises - Status Code 200
    [Documentation]    The crises list with the counters.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    tipo=TENTATIVA_DE_SUICIDIO    descricao=Ingestão de medicamentos.
    ${r}=    POST Crise Saude Mental    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${crise}=    Set Variable    ${r.json()}[uuid]
    ${response}=    GET Crises Saude Mental
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${CRISES_SAUDE_MENTAL_SCHEMA}
    ${ids}=    Evaluate    [c["uuid"] for c in $response.json()["lista"]]
    Should Contain    ${ids}    ${crise}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Internacao Compulsoria Sem Ordem - Status Code 400
    [Documentation]    The compulsory admission needs the court order.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacao}    ${leito}    ${unidade}    ${setor}    ${paciente}    ${matricula}=    Seed Uma Internacao
    ${payload}=    Create Dictionary    modalidade=COMPULSORIA
    ${response}=    POST Acao Internacao    ${internacao}    modalidade    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Internacao Involuntaria - Status Code 200
    [Documentation]    The involuntary admission is communicated to the public prosecutor.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacao}    ${leito}    ${unidade}    ${setor}    ${paciente}    ${matricula}=    Seed Uma Internacao
    ${modalidade}=    Create Dictionary    modalidade=INVOLUNTARIA
    ${r1}=    POST Acao Internacao    ${internacao}    modalidade    ${modalidade}
    Should Be Equal As Integers    ${r1.status_code}    200
    Should Be Equal    ${r1.json()}[comunicacaoMpPendente]    ADMISSAO
    ${comunicacao}=    Create Dictionary    momento=ADMISSAO    responsavelMatricula=${matricula}    protocolo=MP-001
    ${r2}=    POST Acao Internacao    ${internacao}    comunicacao-mp    ${comunicacao}
    Should Be Equal As Integers    ${r2.status_code}    200
    Length Should Be    ${r2.json()}[comunicacoesMp]    1
    ${pendente}=    Evaluate    $r2.json().get("comunicacaoMpPendente")
    Should Be Equal    ${pendente}    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental Risco Crise - By HTTP Status Code
    [Documentation]    Executes the SM3 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-risco'    Saude Mental - Risco - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-risco'    Saude Mental - Risco Sem Medidas - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-crise'    Saude Mental - Crise - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-encerrar'    Saude Mental - Encerrar Sem Pos Crise - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-notificacao'    Saude Mental - Notificacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-crises'    Saude Mental - Crises - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-compulsoria'    Saude Mental - Internacao Compulsoria Sem Ordem - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-involuntaria'    Saude Mental - Internacao Involuntaria - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Saude Mental Risco Crise test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
