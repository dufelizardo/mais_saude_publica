*** Settings ***
Resource    ../../../src/scenario/administracao_medicamento/retificar_administracao_medicamento/retificar_administracao_medicamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST RetificarAdministracaoMedicamento
Metadata    Test Suite Description        This test suite validates the POST /api/v1/administracao-medicamento/{uuid}/retificacao endpoint (ADR-0064) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RetificarAdministracaoMedicamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST RetificarAdministracaoMedicamento - HTTP 201 CREATED
    [Documentation]    Test case to validate that a retification returns the previous quantity to the Lote before taking the new one.
    [Tags]    POST    RetificarAdministracaoMedicamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - RETIFICACAO - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST RetificarAdministracaoMedicamento - HTTP 422 UNPROCESSABLE ENTITY (Versao Ja Retificada)
    [Documentation]    Test case to validate that a version already retified cannot be retified again.
    [Tags]    POST    RetificarAdministracaoMedicamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ADMINISTRACAO MEDICAMENTO - RETIFICACAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
