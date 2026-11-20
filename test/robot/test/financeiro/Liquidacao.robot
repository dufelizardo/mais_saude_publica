*** Settings ***
Resource    ../../src/scenario/financeiro/liquidacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Liquidacao
Metadata    Test Suite Description        Fila a liquidar, liquidação, pagamento, diárias do TFD e execução (ADR-0219).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Liquidacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Liquidacao Do Recebimento - HTTP 422 409 E 201
    [Documentation]    Acima do atesto dá 422; a liquidação com o ISS; repetida dá 409.
    [Tags]    Liquidacao    HTTP422    HTTP409    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LIQUIDACAO    RECEBIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Ordem Cronologica - HTTP 422 E 200
    [Documentation]    Pagar antes da vencida da mesma fonte pede justificativa.
    [Tags]    Liquidacao    HTTP422    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LIQUIDACAO    CRONOLOGICA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Diaria Do Tfd - HTTP 201 E 409
    [Documentation]    A diária pelo CPF do paciente; a repetida dá 409.
    [Tags]    Liquidacao    HTTP201    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LIQUIDACAO    DIARIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Execucao E Restos - HTTP 200
    [Documentation]    A execução e os restos a pagar respondem.
    [Tags]    Liquidacao    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LIQUIDACAO    EXECUCAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

