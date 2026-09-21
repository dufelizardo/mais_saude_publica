*** Settings ***
Resource    ../../../src/scenario/setor/create_setor/create_setor_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Setor
Metadata    Test Suite Description        This test suite validates the POST create Setor endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateSetor    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-21
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Setor - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Setor endpoint with HTTP 201 CREATED response.
    [Tags]    POST    CreateSetor    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SETOR - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Setor - HTTP 404 NOT FOUND (Unidade Inexistente)
    [Documentation]    Test case to validate the POST create Setor endpoint with HTTP 404 NOT FOUND response,
    ...    when unidadeId points at a Unidade de Saúde that doesn't exist.
    [Tags]    POST    CreateSetor    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SETOR - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Setor - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Setor endpoint with HTTP 400 BAD REQUEST response,
    ...    when required fields (unidadeId, nome, codigo, tipo, ativo) are left blank/omitted.
    [Tags]    POST    CreateSetor    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SETOR - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
