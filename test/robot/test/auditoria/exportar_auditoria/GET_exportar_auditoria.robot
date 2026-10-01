*** Settings ***
Resource    ../../../src/scenario/auditoria/exportar_auditoria/exportar_auditoria_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET ExportarAuditoria
Metadata    Test Suite Description        Validates GET /api/v1/auditoria/exportacao and /politica (ADR-0082).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    ExportarAuditoria    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET ExportarAuditoria - HTTP 200 OK
    [Documentation]    CSV do último dia.
    [Tags]    GET    ExportarAuditoria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUDITORIA - EXPORTAR - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET ExportarAuditoria - HTTP 400 BAD REQUEST
    [Documentation]    Período invertido.
    [Tags]    GET    ExportarAuditoria    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUDITORIA - EXPORTAR - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate GET PoliticaAuditoria - HTTP 200 OK
    [Documentation]    Prazo de retenção.
    [Tags]    GET    ExportarAuditoria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUDITORIA - POLITICA - GET
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
