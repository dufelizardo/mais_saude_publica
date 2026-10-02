*** Settings ***
Resource    ../../../src/scenario/auditoria/alerta_auditoria/alerta_auditoria_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - AlertaAuditoria
Metadata    Test Suite Description        This test suite validates the /api/v1/auditoria/alerta/ endpoints (ADR-0096) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               AlertaAuditoria    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-02
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate AlertaAuditoria - HTTP 200 (Login Recusado Vira Alerta)
    [Documentation]    Cinco logins recusados viram um alerta.
    [Tags]    AlertaAuditoria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ALERTA AUDITORIA    200-login
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate AlertaAuditoria - HTTP 200 (Lista E Resumo)
    [Documentation]    Lista com filtro e resumo.
    [Tags]    AlertaAuditoria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ALERTA AUDITORIA    200-lista
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate AlertaAuditoria - HTTP 404 (Inexistente)
    [Documentation]    Alerta que não existe.
    [Tags]    AlertaAuditoria    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ALERTA AUDITORIA    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate AlertaAuditoria - HTTP 200 (Analise E Repeticao)
    [Documentation]    Análise procedente e segunda análise recusada.
    [Tags]    AlertaAuditoria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ALERTA AUDITORIA    200-analise
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate AlertaAuditoria - HTTP 400 (Parecer Curto)
    [Documentation]    Parecer com menos de 10 caracteres.
    [Tags]    AlertaAuditoria    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ALERTA AUDITORIA    400-analise
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
