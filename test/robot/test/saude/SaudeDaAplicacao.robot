*** Settings ***
Resource    ../../src/scenario/saude/saude_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Saude Da Aplicacao
Metadata    Test Suite Description        This test suite validates the health routes used by the Kubernetes probes (ADR-0145).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               SaudeDaAplicacao    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Saude Da Aplicacao - Health
    [Documentation]    The app health answers UP, with no details.
    [Tags]    SaudeDaAplicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DA APLICACAO    200-health
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Saude Da Aplicacao - Liveness
    [Documentation]    The liveness group answers UP.
    [Tags]    SaudeDaAplicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DA APLICACAO    200-liveness
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Saude Da Aplicacao - Readiness
    [Documentation]    The readiness group (with the database) answers UP.
    [Tags]    SaudeDaAplicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DA APLICACAO    200-readiness
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Saude Da Aplicacao - Env Fechado
    [Documentation]    The rest of the Actuator is not exposed.
    [Tags]    SaudeDaAplicacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DA APLICACAO    fechado-env
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
