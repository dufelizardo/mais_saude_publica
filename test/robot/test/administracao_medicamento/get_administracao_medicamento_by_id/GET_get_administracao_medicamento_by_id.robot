*** Settings ***
Resource    ../../../src/scenario/administracao_medicamento/get_administracao_medicamento_by_id/get_administracao_medicamento_by_id_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET GetAdministracaoMedicamentoById
Metadata    Test Suite Description        This test suite validates the GET /api/v1/administracao-medicamento/{uuid} endpoint (ADR-0064) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetAdministracaoMedicamentoById    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET GetAdministracaoMedicamentoById - HTTP 200 OK
    [Documentation]    Test case to validate fetching an existing AdministracaoMedicamento by id.
    [Tags]    GET    GetAdministracaoMedicamentoById    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - GET BY ID - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET GetAdministracaoMedicamentoById - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate that a non-existent AdministracaoMedicamento returns HTTP 404.
    [Tags]    GET    GetAdministracaoMedicamentoById    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - GET BY ID - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
