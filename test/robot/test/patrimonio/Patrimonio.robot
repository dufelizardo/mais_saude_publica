*** Settings ***
Resource    ../../src/scenario/patrimonio/patrimonio_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Patrimonio
Metadata    Test Suite Description        Bens, tombamento, responsáveis e transferências (ADR-0215).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Patrimonio    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Cadastro Do Bem - HTTP 400 E 201
    [Documentation]    Aquisição futura dá 400; o bem entra tombado e em uso.
    [Tags]    Patrimonio    HTTP400    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATRIMONIO    CADASTRO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Troca Do Responsavel - HTTP 422 E 200
    [Documentation]    A mesma pessoa dá 422; a troca encerra o termo anterior.
    [Tags]    Patrimonio    HTTP422    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATRIMONIO    RESPONSAVEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Transferencia Em Duas Etapas - HTTP 409 E 200
    [Documentation]    Uma transferência por vez; o destino aceita e o bem muda de unidade.
    [Tags]    Patrimonio    HTTP409    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATRIMONIO    TRANSFERENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Bem Comprado A Tombar - HTTP 200
    [Documentation]    O bem permanente aceito no recebimento nasce a tombar e é tombado.
    [Tags]    Patrimonio    HTTP200    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PATRIMONIO    A_TOMBAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

