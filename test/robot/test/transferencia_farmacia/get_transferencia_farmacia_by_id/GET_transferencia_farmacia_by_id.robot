*** Settings ***
Resource    ../../../src/scenario/transferencia_farmacia/get_transferencia_farmacia_by_id/get_transferencia_farmacia_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Transferencia Farmacia By Id
Metadata    Test Suite Description        This test suite validates the GET stock transfer by id endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetTransferenciaFarmaciaById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-29
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Transferencia Farmacia By Id - HTTP 200 OK
    [Documentation]    Test case to validate fetching an existing transfer by id.
    [Tags]    GET    GetTransferenciaFarmaciaById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Transferencia Farmacia By Id - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent transfer returns HTTP 404.
    [Tags]    GET    GetTransferenciaFarmaciaById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRANSFERENCIA FARMACIA - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
