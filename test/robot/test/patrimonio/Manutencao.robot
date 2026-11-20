*** Settings ***
Resource    ../../src/scenario/patrimonio/manutencao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Manutencao
Metadata    Test Suite Description        Planos de manutenção, chamados e ordens de serviço (ADR-0216).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Manutencao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Chamado E Execucao Interna - HTTP 400 E 200
    [Documentation]    Sem técnico dá 400; a conclusão devolve o bem ao uso.
    [Tags]    Manutencao    HTTP400    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MANUTENCAO    EXECUCAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Plano Vencido - HTTP 400 409 E 200
    [Documentation]    Corretiva sem plano, um plano por tipo e a ordem gerada uma vez.
    [Tags]    Manutencao    HTTP400    HTTP409    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MANUTENCAO    PLANO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Calibracao - HTTP 400 E 200
    [Documentation]    O certificado e a validade são obrigatórios na calibração.
    [Tags]    Manutencao    HTTP400    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MANUTENCAO    CALIBRACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Cancelamento - HTTP 200 E 422
    [Documentation]    O cancelamento da execução devolve o bem; repetido dá 422.
    [Tags]    Manutencao    HTTP200    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MANUTENCAO    CANCELAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

