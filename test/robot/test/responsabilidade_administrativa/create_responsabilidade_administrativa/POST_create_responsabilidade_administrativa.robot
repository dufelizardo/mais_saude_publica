*** Settings ***
Resource    ../../../src/scenario/responsabilidade_administrativa/create_responsabilidade_administrativa/create_responsabilidade_administrativa_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Responsabilidade Administrativa
Metadata    Test Suite Description        This test suite validates the POST create Responsabilidade Administrativa endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateResponsabilidadeAdministrativa    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Responsabilidade Administrativa - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Responsabilidade Administrativa endpoint with HTTP
    ...    201 CREATED response.
    [Tags]    POST    CreateResponsabilidadeAdministrativa    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Responsabilidade Administrativa - HTTP 404 NOT FOUND (Profissional Inexistente)
    [Documentation]    Test case to validate the POST create Responsabilidade Administrativa endpoint with HTTP
    ...    404 NOT FOUND response, when matriculaProfissional points at a Profissional that doesn't exist.
    [Tags]    POST    CreateResponsabilidadeAdministrativa    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Responsabilidade Administrativa - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Responsabilidade Administrativa endpoint with HTTP
    ...    400 BAD REQUEST response, when required fields are left blank/omitted.
    [Tags]    POST    CreateResponsabilidadeAdministrativa    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Responsabilidade Administrativa - HTTP 404 NOT FOUND (Setor Inexistente)
    [Documentation]    Test case to validate the POST create Responsabilidade Administrativa endpoint with HTTP
    ...    404 NOT FOUND response, when setorId points at a Setor that doesn't exist.
    [Tags]    POST    CreateResponsabilidadeAdministrativa    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - CREATE - POST - Setor Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
