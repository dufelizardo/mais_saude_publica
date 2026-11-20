*** Settings ***
Resource    ../../src/scenario/patrimonio/inventario_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Inventario
Metadata    Test Suite Description        Inventário, baixa e painel do Patrimônio (ADR-0217).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Inventario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Abertura Do Inventario - HTTP 400 409 E 201
    [Documentation]    Comissão incompleta dá 400; um inventário aberto por unidade.
    [Tags]    Inventario    HTTP400    HTTP409    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVENTARIO    ABERTURA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Conferencia E Fechamento - HTTP 422 E 200
    [Documentation]    Fechar com bem a conferir dá 422; o não encontrado fica com a baixa sugerida.
    [Tags]    Inventario    HTTP422    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVENTARIO    FECHAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Baixa - HTTP 400 201 E 422
    [Documentation]    Furto sem boletim dá 400; a baixa abaixo da alçada vale na hora.
    [Tags]    Inventario    HTTP400    HTTP201    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVENTARIO    BAIXA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Painel Do Patrimonio - HTTP 200
    [Documentation]    O painel responde com os totais.
    [Tags]    Inventario    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVENTARIO    PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

