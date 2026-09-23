*** Settings ***
Resource    ../../../src/scenario/capacidade_administrativa/update_capacidade_administrativa/update_capacidade_administrativa_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Capacidade Administrativa
Metadata    Test Suite Description        This test suite validates the PATCH update Capacidade Administrativa endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateCapacidadeAdministrativa    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Capacidade Administrativa - HTTP 200 OK (Edita Campos E Desativa)
    [Documentation]    Test case to validate the PATCH update Capacidade Administrativa endpoint with HTTP 200 OK
    ...    response, editing nome and setting ativo=False (there's no dedicated des-habilitar endpoint).
    [Tags]    PATCH    UpdateCapacidadeAdministrativa    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAPACIDADE ADMINISTRATIVA - UPDATE - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Capacidade Administrativa - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update Capacidade Administrativa endpoint with HTTP 404 NOT
    ...    FOUND response, when the uuid in the path doesn't exist.
    [Tags]    PATCH    UpdateCapacidadeAdministrativa    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CAPACIDADE ADMINISTRATIVA - UPDATE - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
