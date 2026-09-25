*** Settings ***
Resource    ../../../src/scenario/processo_administrativo/get_processo_administrativo_by_id/get_processo_administrativo_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Processo Administrativo By Id
Metadata    Test Suite Description        This test suite validates the GET Processo Administrativo by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetProcessoAdministrativoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Processo Administrativo By Id - HTTP 200 OK
    [Documentation]    Test case to validate the GET Processo Administrativo by id endpoint with HTTP 200 OK
    ...    response.
    [Tags]    GET    GetProcessoAdministrativoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO ADMINISTRATIVO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Processo Administrativo By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Processo Administrativo by id endpoint with HTTP 404 NOT
    ...    FOUND response.
    [Tags]    GET    GetProcessoAdministrativoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCESSO ADMINISTRATIVO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
