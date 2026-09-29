*** Settings ***
Resource    ../../../src/scenario/administracao_medicamento/create_administracao_medicamento/create_administracao_medicamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CreateAdministracaoMedicamento
Metadata    Test Suite Description        This test suite validates the POST /api/v1/administracao-medicamento/ endpoint (ADR-0064) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateAdministracaoMedicamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CreateAdministracaoMedicamento - HTTP 201 CREATED
    [Documentation]    Test case to validate that administering a prescribed medication takes the quantity from the unit Lote.
    [Tags]    POST    CreateAdministracaoMedicamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CreateAdministracaoMedicamento - HTTP 400 BAD REQUEST (Administrado Sem Lote)
    [Documentation]    Test case to validate that an ADMINISTRADO check requires the Lote.
    [Tags]    POST    CreateAdministracaoMedicamento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST CreateAdministracaoMedicamento - HTTP 422 UNPROCESSABLE ENTITY (Maior Que O Saldo)
    [Documentation]    Test case to validate that administering more than the Lote balance is rejected.
    [Tags]    POST    CreateAdministracaoMedicamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - CREATE - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
