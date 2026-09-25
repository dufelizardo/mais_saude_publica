*** Settings ***
Resource    ../../../src/scenario/processo_administrativo/create_processo_administrativo/create_processo_administrativo_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Processo Administrativo
Metadata    Test Suite Description        This test suite validates the POST create Processo Administrativo endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateProcessoAdministrativo    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Processo Administrativo - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Processo Administrativo endpoint with HTTP 201
    ...    CREATED response.
    [Tags]    POST    CreateProcessoAdministrativo    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO ADMINISTRATIVO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Processo Administrativo - HTTP 404 NOT FOUND (Capacidade Inexistente)
    [Documentation]    Test case to validate the POST create Processo Administrativo endpoint with HTTP 404 NOT
    ...    FOUND response, when capacidadeId points at a Capacidade Administrativa that doesn't exist.
    [Tags]    POST    CreateProcessoAdministrativo    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO ADMINISTRATIVO - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Processo Administrativo - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Processo Administrativo endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields (capacidadeId, codigo, nome, ativo) are left blank/omitted.
    [Tags]    POST    CreateProcessoAdministrativo    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO ADMINISTRATIVO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
