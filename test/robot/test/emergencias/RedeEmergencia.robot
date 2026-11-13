*** Settings ***
Resource    ../../src/scenario/emergencias/rede_emergencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET/POST/DELETE Rede na Emergencia
Metadata    Test Suite Description        Onde tem na rede, compromissos nas unidades afetadas e prioridade da emergência na Regulação (ADR-0194).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    POST    DELETE    RedeEmergencia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Onde Tem - HTTP 400 E 200
    [Documentation]    O material com saldo aparece na unidade; sem filtro, 400; os compromissos respondem.
    [Tags]    GET    RedeEmergencia    HTTP400    HTTP200
    REDE NA EMERGENCIA    ONDE_TEM

CT-002 - Validate Prioridade Na Regulacao - HTTP 422 409 E 200
    [Documentation]    Fora da emergência, 422; do deslocado, sobe na fila com a marca; duas vezes, 409; retirar duas vezes, 422.
    [Tags]    POST    DELETE    RedeEmergencia    HTTP422    HTTP409    HTTP200
    REDE NA EMERGENCIA    PRIORIDADE
