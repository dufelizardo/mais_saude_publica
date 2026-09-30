*** Settings ***
Resource    ../../../src/scenario/prontuario/create_acesso_justificado/create_acesso_justificado_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Acesso Justificado
Metadata    Test Suite Description        This test suite validates the justified access to a Paciente's Prontuário (ADR-0076).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateAcessoJustificado    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Acesso Justificado - HTTP 201 CREATED
    [Documentation]    Test case to validate the justified access with HTTP 201 CREATED response.
    [Tags]    POST    CreateAcessoJustificado    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRONTUARIO - CREATE ACESSO JUSTIFICADO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Acesso Justificado - HTTP 400 BAD REQUEST
    [Documentation]    Test case to validate the justified access with a too short justification, HTTP 400.
    [Tags]    POST    CreateAcessoJustificado    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRONTUARIO - CREATE ACESSO JUSTIFICADO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Acesso Justificado - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the justified access for a non-existent Paciente, HTTP 404.
    [Tags]    POST    CreateAcessoJustificado    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PRONTUARIO - CREATE ACESSO JUSTIFICADO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
