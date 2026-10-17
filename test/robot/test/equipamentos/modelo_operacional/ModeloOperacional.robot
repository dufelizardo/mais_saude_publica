*** Settings ***
Resource    ../../../src/scenario/equipamentos/modelo_operacional/modelo_operacional_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - ModeloOperacional
Metadata    Test Suite Description        This test suite validates the operational profiles and care capabilities (ADR-0117) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               ModeloOperacional    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate ModeloOperacional - HTTP 200 (Catalogo)
    [Documentation]    Carga inicial com os dez perfis e as capacidades agrupadas.
    [Tags]    ModeloOperacional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    200-catalogo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate ModeloOperacional - HTTP 200 (Unidade)
    [Documentation]    Perfil, capacidade do grupo e transversal num CAPS; a rede mostra o perfil.
    [Tags]    ModeloOperacional    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    200-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate ModeloOperacional - HTTP 422 (Capacidade Sem Perfil)
    [Documentation]    Capacidade de um grupo sem o perfil vigente na unidade.
    [Tags]    ModeloOperacional    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    422-grupo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate ModeloOperacional - HTTP 409 (Perfil Repetido)
    [Documentation]    O mesmo perfil vigente duas vezes.
    [Tags]    ModeloOperacional    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    409-perfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate ModeloOperacional - HTTP 200 (Encerramento)
    [Documentation]    O perfil só encerra depois das capacidades do grupo; os dois ficam no histórico.
    [Tags]    ModeloOperacional    HTTP200    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    200-encerramento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate ModeloOperacional - HTTP 422 (Nivel De Gestao)
    [Documentation]    Os níveis de gestão não recebem perfil.
    [Tags]    ModeloOperacional    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    422-gestao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate ModeloOperacional - HTTP 409 (Codigo Repetido)
    [Documentation]    Código repetido no catálogo de perfis.
    [Tags]    ModeloOperacional    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    409-catalogo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate ModeloOperacional - HTTP 404 (Inexistente)
    [Documentation]    Modelo operacional de uma unidade que não existe.
    [Tags]    ModeloOperacional    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    MODELO OPERACIONAL    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
