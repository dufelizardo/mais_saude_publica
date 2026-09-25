*** Settings ***
Resource    ../../../src/scenario/responsabilidade_administrativa/encerrar_responsabilidade_administrativa/encerrar_responsabilidade_administrativa_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Encerrar Responsabilidade Administrativa
Metadata    Test Suite Description        This test suite validates the PATCH encerrar Responsabilidade Administrativa endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    EncerrarResponsabilidadeAdministrativa    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Encerrar Responsabilidade Administrativa - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH encerrar Responsabilidade Administrativa endpoint with
    ...    HTTP 200 OK response.
    [Tags]    PATCH    EncerrarResponsabilidadeAdministrativa    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - ENCERRAR - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Encerrar Responsabilidade Administrativa - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH encerrar Responsabilidade Administrativa endpoint with
    ...    HTTP 404 NOT FOUND response, when the uuid in the path doesn't exist.
    [Tags]    PATCH    EncerrarResponsabilidadeAdministrativa    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - ENCERRAR - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH Encerrar Responsabilidade Administrativa - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the PATCH encerrar Responsabilidade Administrativa endpoint with
    ...    HTTP 409 CONFLICT response, when the responsabilidade was already encerrada.
    [Tags]    PATCH    EncerrarResponsabilidadeAdministrativa    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - ENCERRAR - PATCH    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
