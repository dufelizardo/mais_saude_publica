*** Settings ***
Resource    ../../src/scenario/qualidade/indicadores_qualidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Indicadores de Qualidade
Metadata    Test Suite Description        Indicadores com metas, checklists versionados, auditoria de adesão e a origem indicador (ADR-0205).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    IndicadoresQualidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Painel - HTTP 200
    [Documentation]    Os sete indicadores e a adesão por checklist, sem base sem dados, com a série de 12 meses.
    [Tags]    GET    IndicadoresQualidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES QUALIDADE    PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Meta - HTTP 201
    [Documentation]    A meta vale do mês em diante; repetir o mês dá 422; a adesão sem protocolo dá 400.
    [Tags]    POST    GET    IndicadoresQualidade    HTTP201    HTTP400    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES QUALIDADE    META
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Auditoria de Protocolo - HTTP 201
    [Documentation]    Todos os itens respondidos; a adesão é conformes sobre aplicáveis.
    [Tags]    POST    IndicadoresQualidade    HTTP201    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES QUALIDADE    AUDITORIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Checklist Versionado - HTTP 201
    [Documentation]    Código único (409) e a nova versão.
    [Tags]    POST    IndicadoresQualidade    HTTP201    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES QUALIDADE    CHECKLIST
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Nao Conformidade do Indicador - HTTP 201
    [Documentation]    A origem indicador pede o indicador e a competência; a segunda em aberto do mês dá 409.
    [Tags]    POST    IndicadoresQualidade    HTTP201    HTTP400    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INDICADORES QUALIDADE    NAO_CONFORMIDADE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
