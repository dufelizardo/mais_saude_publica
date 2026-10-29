*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/desmobilizacao_pom.resource
Resource    coordenacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Desmobilização e relatório pós-evento (ADR-0159).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Emergencia Com Plano Ativo
    [Documentation]    An emergency with an active plan, a mobilized professional and an open action; returns the uuid and the plan code.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    ${matricula}=    Seed A Profissional Matricula
    ${destino}=    Seed A Unidade De Saude Uuid
    Mobilizar Profissional    ${em.json()}[uuid]    ${matricula}    ${destino}
    Nova Acao Avulsa    ${em.json()}[uuid]
    ${detalhe}=    GET Emergencia    ${em.json()}[uuid]
    RETURN    ${em.json()}[uuid]    ${detalhe.json()}[planos][0][codigo]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizar
    [Documentation]    Starts the demobilization; returns the response.
    [Arguments]    ${emergencia}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    responsavel=Secretário de Saúde    justificativa=Águas baixando
    ${r}=    POST Desmobilizacao    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cancelar Restantes
    [Documentation]    Cancels the remaining actions.
    [Arguments]    ${emergencia}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    motivo=Desnecessárias após a cheia
    ${r}=    POST Cancelar Acoes Restantes    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encerrar
    [Documentation]    Ends the emergency.
    [Arguments]    ${emergencia}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    motivo=Rede normalizada
    ${r}=    POST Encerrar Emergencia E5    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rascunho Do Relatorio
    [Documentation]    Saves the draft with a recommendation for the plan.
    [Arguments]    ${emergencia}    ${plano}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${rec}=    Create Dictionary    descricao=Abrir o abrigo já no nível 1    planoCodigo=${plano}
    ${recs}=    Create List    ${rec}
    ${payload}=    Create Dictionary    resumo=Cheia de três dias    problemas=Abrigo tardio    licoes=Antecipar o abrigo    recomendacoes=${recs}
    ${r}=    PUT Relatorio Pos Evento    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - Iniciar - Status Code 200
    [Documentation]    Everything open is released at once.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    ${r}=    Desmobilizar    ${em}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    EM_DESMOBILIZACAO
    Should Be Equal As Integers    ${r.json()}[desmobilizacao][profissionaisLiberados]    1
    Should Be Equal As Integers    ${r.json()}[desmobilizacao][planosDesativados]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - Acao Nova - Status Code 422
    [Documentation]    No new action during demobilization.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    ${r}=    Nova Acao Avulsa    ${em}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - De Novo - Status Code 422
    [Documentation]    Demobilization starts once.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    ${r}=    Desmobilizar    ${em}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - Encerrar Com Acao Aberta - Status Code 422
    [Documentation]    Open actions block the end.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    ${r}=    Encerrar    ${em}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - Cancelar Restantes - Status Code 200
    [Documentation]    The remaining actions are cancelled.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    ${r}=    Cancelar Restantes    ${em}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Empty    ${r.json()}[pendencias]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - Encerrar - Status Code 200
    [Documentation]    After the demobilization, the emergency ends.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    Cancelar Restantes    ${em}
    ${r}=    Encerrar    ${em}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ENCERRADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorio - Na Ativa - Status Code 422
    [Documentation]    The report starts at the demobilization.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    ${r}=    Rascunho Do Relatorio    ${em}    ${plano}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorio - Rascunho - Status Code 200
    [Documentation]    The draft with the computed numbers.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    ${r}=    Rascunho Do Relatorio    ${em}    ${plano}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${RELATORIO_POS_EVENTO_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    RASCUNHO
    Should Be Equal As Integers    ${r.json()}[indicadores][profissionaisMobilizados]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorio - Revisao Do Plano - Status Code 200
    [Documentation]    The recommendation opens version 2 of the plan.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    ${r}=    Rascunho Do Relatorio    ${em}    ${plano}
    ${v}=    POST Revisar Plano Da Recomendacao    ${em}    ${r.json()}[recomendacoes][0][uuid]
    Should Be Equal As Integers    ${v.status_code}    200
    Should Be Equal As Integers    ${v.json()}[recomendacoes][0][planoRevisaoVersao]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorio - Finalizar - Status Code 200
    [Documentation]    Finalized after the emergency ends.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    Rascunho Do Relatorio    ${em}    ${plano}
    Cancelar Restantes    ${em}
    Encerrar    ${em}
    ${r}=    POST Finalizar Relatorio    ${em}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    FINALIZADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Relatorio - Csv - Status Code 200
    [Documentation]    The numbers in CSV.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    ${r}=    GET Relatorio Csv    ${em}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Contain    ${r.text}    Nível máximo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Desmobilizacao - By HTTP Status Code
    [Documentation]    Executes the E5 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-desmobilizacao'    Desmobilizacao - Iniciar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-acao-na-desmobilizacao'    Desmobilizacao - Acao Nova - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-desmobilizar-de-novo'    Desmobilizacao - De Novo - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-encerrar-com-acao-aberta'    Desmobilizacao - Encerrar Com Acao Aberta - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-cancelar-restantes'    Desmobilizacao - Cancelar Restantes - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-encerrar'    Desmobilizacao - Encerrar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-relatorio-na-ativa'    Relatorio - Na Ativa - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-relatorio-rascunho'    Relatorio - Rascunho - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-revisao-do-plano'    Relatorio - Revisao Do Plano - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-finalizar'    Relatorio - Finalizar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-csv'    Relatorio - Csv - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Desmobilizacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
