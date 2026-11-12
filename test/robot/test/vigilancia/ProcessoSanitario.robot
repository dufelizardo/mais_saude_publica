*** Settings ***
Resource    ../../src/scenario/vigilancia/processo_sanitario_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Processo Sanitário
Metadata    Test Suite Description        Auto de infração, processo administrativo sanitário, medidas e verificação pública (ADR-0200).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    ProcessoSanitario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Rito Com Encerramento - HTTP 200
    [Documentation]    Defesa intempestiva, penalidades, ciência e encerramento: a licença fica suspensa.
    [Tags]    POST    ProcessoSanitario    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO SANITARIO    RITO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Decisao Antes Do Prazo - HTTP 422
    [Documentation]    Sem defesa, a decisão espera os 15 dias.
    [Tags]    POST    ProcessoSanitario    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO SANITARIO    PRAZO_DEFESA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Verificacao Publica Do Termo - HTTP 200
    [Documentation]    O código do auto confere sem login.
    [Tags]    GET    ProcessoSanitario    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO SANITARIO    VERIFICACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Interdicao Cautelar - HTTP 201
    [Documentation]    Até 90 dias (400 além) e a desinterdição com o termo.
    [Tags]    POST    ProcessoSanitario    HTTP201    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO SANITARIO    INTERDICAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
