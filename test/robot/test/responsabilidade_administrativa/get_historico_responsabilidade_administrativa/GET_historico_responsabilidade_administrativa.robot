*** Settings ***
Resource    ../../../src/scenario/responsabilidade_administrativa/get_historico_responsabilidade_administrativa/get_historico_responsabilidade_administrativa_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Historico Responsabilidade Administrativa
Metadata    Test Suite Description        This test suite validates the GET Responsabilidade Administrativa histórico endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetHistoricoResponsabilidadeAdministrativa    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Historico Responsabilidade Administrativa - HTTP 200 OK
    [Documentation]    Test case to validate the GET Responsabilidade Administrativa histórico endpoint with HTTP
    ...    200 OK response.
    [Tags]    GET    GetHistoricoResponsabilidadeAdministrativa    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - GET HISTORICO - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Historico Responsabilidade Administrativa - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Responsabilidade Administrativa histórico endpoint with HTTP
    ...    404 NOT FOUND response, when the profissional has no responsabilidades.
    [Tags]    GET    GetHistoricoResponsabilidadeAdministrativa    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    RESPONSABILIDADE ADMINISTRATIVA - GET HISTORICO - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
