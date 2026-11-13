*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/qualidade/indicadores_qualidade_pom.resource
Resource    ../../pom/qualidade/nao_conformidade_pom.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os indicadores de qualidade, as metas, os checklists versionados, a auditoria de adesão e a origem indicador (ADR-0205).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Versao Higiene Das Maos
    [Documentation]    Returns the version uuid and the uuid of the seeded hand hygiene checklist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Checklists Protocolo
    Should Be Equal As Integers    ${r.status_code}    200
    ${versoes}=    Evaluate    [c['versaoId'] for c in $r.json() if c['codigo'] == 'HIGIENE_DAS_MAOS']
    ${ids}=    Evaluate    [c['uuid'] for c in $r.json() if c['codigo'] == 'HIGIENE_DAS_MAOS']
    RETURN    ${versoes}[0]    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Qualidade - Painel - Status Code 200
    [Documentation]    The panel brings the seven indicators, adherence once per checklist, with the 12-month series.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Indicadores Qualidade    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    ${linhas}=    Get Length    ${r.json()}[linhas]
    Should Be True    ${linhas} >= 8
    ${quedas}=    Evaluate    [l for l in $r.json()['linhas'] if l['indicador'] == 'TAXA_QUEDAS'][0]
    Should Be Equal    ${quedas}[situacao]    SEM_BASE
    Length Should Be    ${quedas}[serie]    12
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Qualidade - Meta - Status Code 201
    [Documentation]    A goal from the month on; the same month again is 422; adherence without the protocol is 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    unidadeId=${unidade}    indicador=TAXA_QUEDAS    valor=${3}
    ${r}=    POST Meta Qualidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[vigenteAte]    ${None}
    ${r}=    POST Meta Qualidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${adesao}=    Create Dictionary    unidadeId=${unidade}    indicador=ADESAO_PROTOCOLO    valor=${90}
    ${r}=    POST Meta Qualidade    ${adesao}
    Should Be Equal As Integers    ${r.status_code}    400
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Metas Qualidade    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Length Should Be    ${r.json()}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Qualidade - Auditoria - Status Code 201
    [Documentation]    Every item answered (400 otherwise); adherence is conformant over applicable.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${versao}    ${checklist}=    Versao Higiene Das Maos
    ${respostas}=    Create Dictionary    M1=CONFORME
    ${payload}=    Create Dictionary    unidadeId=${unidade}    versaoId=${versao}    respostas=${respostas}
    ${r}=    POST Auditoria Protocolo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${respostas}=    Create Dictionary    M1=CONFORME    M2=CONFORME    M3=NAO_CONFORME    M4=NAO_SE_APLICA    M5=CONFORME
    Set To Dictionary    ${payload}    respostas=${respostas}
    ${r}=    POST Auditoria Protocolo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[aplicaveis]    4
    Should Be Equal As Numbers    ${r.json()}[adesao]    75.0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Qualidade - Checklist - Status Code 201
    [Documentation]    A unique code (409 when repeated) and a new version keeps the old one.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER]
    ${item}=    Create Dictionary    codigo=P1    texto=Pulseira com dois identificadores
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    codigo=ROBOT_${sufixo}    nome=Identificação ${sufixo}    itens=${itens}
    ${r}=    POST Checklist Protocolo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[versao]    1
    ${id}=    Set Variable    ${r.json()}[uuid]
    ${r}=    POST Checklist Protocolo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    ${item2}=    Create Dictionary    codigo=P2    texto=Conferência antes do medicamento
    Append To List    ${itens}    ${item2}
    ${versao}=    Create Dictionary    itens=${itens}
    ${r}=    POST Versao Checklist Protocolo    ${id}    ${versao}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[versao]    2
    Length Should Be    ${r.json()}[itens]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Qualidade - Nao Conformidade Do Indicador - Status Code 201
    [Documentation]    The indicator origin asks for the indicator and the month; a second open one of the same month is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${competencia}=    Get Current Date    increment=-31 days    result_format=%Y-%m-01
    ${payload}=    Create Dictionary    unidadeId=${unidade}    origem=INDICADOR    descricao=Quedas acima da meta.    gravidade=MAIOR
    ...    responsavelMatricula=${matricula}    indicador=TAXA_QUEDAS
    ${r}=    POST Nao Conformidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    Set To Dictionary    ${payload}    competencia=${competencia}
    ${r}=    POST Nao Conformidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[indicador]    TAXA_QUEDAS
    Should Be Equal    ${r.json()}[competencia]    ${competencia}
    ${r}=    POST Nao Conformidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores Qualidade - By Cenario
    [Documentation]    Executes the quality indicator scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'PAINEL'
        Indicadores Qualidade - Painel - Status Code 200
    ELSE IF    '${caso}' == 'META'
        Indicadores Qualidade - Meta - Status Code 201
    ELSE IF    '${caso}' == 'AUDITORIA'
        Indicadores Qualidade - Auditoria - Status Code 201
    ELSE IF    '${caso}' == 'CHECKLIST'
        Indicadores Qualidade - Checklist - Status Code 201
    ELSE IF    '${caso}' == 'NAO_CONFORMIDADE'
        Indicadores Qualidade - Nao Conformidade Do Indicador - Status Code 201
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Indicadores de Qualidade.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
