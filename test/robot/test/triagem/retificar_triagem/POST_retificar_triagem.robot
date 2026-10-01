*** Settings ***
Resource    ../../../src/scenario/triagem/retificar_triagem/retificar_triagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Retificar Triagem
Metadata    Test Suite Description        This test suite validates the POST Triagem retification endpoint (ADR-0062) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RetificarTriagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Retificar Triagem - HTTP 201 CREATED
    [Documentation]    Test case to validate that retifying a Triagem creates a new version and keeps the original, flagged as retificado.
    [Tags]    POST    RetificarTriagem    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - RETIFICACAO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Retificar Triagem - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that retifying a non-existent Triagem returns HTTP 404.
    [Tags]    POST    RetificarTriagem    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - RETIFICACAO - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Retificar Triagem - HTTP 422 UNPROCESSABLE ENTITY (Versao Ja Retificada)
    [Documentation]    Test case to validate that a Triagem version that was already retified cannot be retified again.
    [Tags]    POST    RetificarTriagem    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRIAGEM - RETIFICACAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
