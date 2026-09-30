*** Settings ***
Resource    ../../../src/scenario/auditoria/consultar_auditoria/consultar_auditoria_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET ConsultarAuditoria
Metadata    Test Suite Description        This test suite validates the GET /api/v1/auditoria/ endpoint (ADR-0071) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    ConsultarAuditoria    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET ConsultarAuditoria - HTTP 200 OK
    [Documentation]    Test case to validate querying the audit trail.
    [Tags]    GET    ConsultarAuditoria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUDITORIA - CONSULTAR - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET ConsultarAuditoria - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate that an inverted period returns HTTP 400.
    [Tags]    GET    ConsultarAuditoria    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUDITORIA - CONSULTAR - GET    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
