*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/epidemia_exposicao_pom.resource
Resource    desmobilizacao_flow.resource
Resource    ../vigilancia/vigilancia_contatos_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Painel da epidemia e exposição ambiental (ADR-0161).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Emergencia Do Tipo
    [Documentation]    A fresh event of the given type turned emergency at level 1; returns the uuid.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${tipo}=    Id Do Tipo Emergencia    ${codigo}
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${evento}=    Create Dictionary    tipoId=${tipo}    fonte=VIGILANCIA    descricao=Robot ${codigo} ${sufixo}
    ${e}=    POST Evento Emergencial    ${evento}
    ${nivel}=    Id Do Nivel Emergencia    1
    ${payload}=    Create Dictionary    nome=${codigo} Robot    nivelId=${nivel}    justificativa=Limiar
    ${r}=    POST Converter Em Emergencia    ${e.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia De Sarampo
    [Documentation]    An epidemic emergency with measles as the disease; returns the uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    EPIDEMIA
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${payload}=    Create Dictionary    agravoId=${sarampo}
    ${r}=    PUT Agravo Da Emergencia    ${em}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${em}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exposicao De Benzeno
    [Documentation]    A water exposure; returns the response.
    [Arguments]    ${emergencia}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    meio=AGUA    agente=Benzeno    fonte=Tanque da indústria    populacaoExposta=${1200}    medidas=Suspender a captação
    ${r}=    POST Exposicao Ambiental    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Painel Sem Agravo - Status Code 422
    [Documentation]    Without a disease the panel is not available.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    EPIDEMIA
    ${r}=    GET Painel Epidemia    ${em}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Definir Agravo - Status Code 200
    [Documentation]    The disease is chosen in the epidemic.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Epidemia De Sarampo
    ${r}=    GET Emergencia    ${em}
    Should Contain    ${r.json()}[agravoNome]    Sarampo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Agravo Outro Perfil - Status Code 422
    [Documentation]    Only the epidemic has a disease.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    INUNDACAO
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${payload}=    Create Dictionary    agravoId=${sarampo}
    ${r}=    PUT Agravo Da Emergencia    ${em}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Painel - Status Code 200
    [Documentation]    The panel brings the weekly curve and the totals.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Epidemia De Sarampo
    Seed Caso De Sarampo
    ${r}=    GET Painel Epidemia    ${em}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PAINEL_EPIDEMIA_SCHEMA}
    Should Be True    ${r.json()}[notificados] >= 1
    Should Be True    ${r.json()}[semanaAtual] >= 1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Painel Conta Notificacao - Status Code 200
    [Documentation]    A new notification enters the panel.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Epidemia De Sarampo
    ${antes}=    GET Painel Epidemia    ${em}
    Seed Caso De Sarampo
    ${depois}=    GET Painel Epidemia    ${em}
    ${esperado}=    Evaluate    $antes.json()['notificados'] + 1
    Should Be Equal As Integers    ${depois.json()}[notificados]    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Exposicao - Status Code 201
    [Documentation]    The exposure is registered in the environmental disaster.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    PRODUTO_PERIGOSO
    ${r}=    Exposicao De Benzeno    ${em}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}[exposicoes][0]    ${EXPOSICAO_AMBIENTAL_SCHEMA}
    Should Be Equal    ${r.json()}[exposicoes][0][agente]    Benzeno
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Exposicao Outro Perfil - Status Code 422
    [Documentation]    Only the environmental disaster has exposures.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    EPIDEMIA
    ${r}=    Exposicao De Benzeno    ${em}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Exposicao Sem Agente - Status Code 400
    [Documentation]    The agent is required.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    PRODUTO_PERIGOSO
    ${payload}=    Create Dictionary    meio=AR    agente=${EMPTY}
    ${r}=    POST Exposicao Ambiental    ${em}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - Relatorio - Status Code 200
    [Documentation]    The report brings the exposures.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    PRODUTO_PERIGOSO
    Exposicao De Benzeno    ${em}
    Desmobilizar    ${em}
    ${r}=    GET Relatorio Pos Evento    ${em}
    Should Be Equal As Integers    ${r.json()}[indicadores][exposicoes]    1
    Should Be Equal As Integers    ${r.json()}[indicadores][populacaoExpostaMaxima]    1200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Epidemia Exposicao - By HTTP Status Code
    [Documentation]    Executes the E6b scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '422-painel-sem-agravo'    Epidemia Exposicao - Painel Sem Agravo - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-definir-agravo'    Epidemia Exposicao - Definir Agravo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-agravo-outro-perfil'    Epidemia Exposicao - Agravo Outro Perfil - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-painel'    Epidemia Exposicao - Painel - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-painel-conta-notificacao'    Epidemia Exposicao - Painel Conta Notificacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-exposicao'    Epidemia Exposicao - Exposicao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-exposicao-outro-perfil'    Epidemia Exposicao - Exposicao Outro Perfil - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-exposicao-sem-agente'    Epidemia Exposicao - Exposicao Sem Agente - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-relatorio'    Epidemia Exposicao - Relatorio - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Epidemia Exposicao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
