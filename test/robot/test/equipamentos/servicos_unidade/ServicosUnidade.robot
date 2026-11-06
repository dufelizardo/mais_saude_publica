*** Settings ***
Resource    ../../../src/scenario/equipamentos/servicos_unidade/servicos_unidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - ServicosUnidade
Metadata    Test Suite Description        This test suite validates the service types and the services of the units (ADR-0165) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               ServicosUnidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-06
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate ServicosUnidade - HTTP 200 (Catalogo)
    [Documentation]    Carga inicial dos tipos, com a capacidade exigida.
    [Tags]    ServicosUnidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    200-catalogo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate ServicosUnidade - HTTP 201 E 409 (Tipo)
    [Documentation]    Cria, recusa código repetido e edita o tipo.
    [Tags]    ServicosUnidade    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    201-tipo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate ServicosUnidade - HTTP 200 (Unidade)
    [Documentation]    Serviço com setor; os leitos vêm do setor.
    [Tags]    ServicosUnidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    200-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate ServicosUnidade - HTTP 422 (Sem Capacidade)
    [Documentation]    O tipo exige capacidade que a unidade não tem.
    [Tags]    ServicosUnidade    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    422-capacidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate ServicosUnidade - HTTP 409 (Setor Ocupado)
    [Documentation]    Um setor em um serviço vigente por vez.
    [Tags]    ServicosUnidade    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    409-setor
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate ServicosUnidade - HTTP 404 (Tipo)
    [Documentation]    Tipo que não existe.
    [Tags]    ServicosUnidade    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    404-tipo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate ServicosUnidade - HTTP 422 E 200 (Encerramento)
    [Documentation]    A capacidade só encerra depois do serviço; o serviço fica no histórico.
    [Tags]    ServicosUnidade    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SERVICOS UNIDADE    422-encerramento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

