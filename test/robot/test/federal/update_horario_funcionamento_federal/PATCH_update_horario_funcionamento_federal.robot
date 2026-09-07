*** Settings ***
Resource    ../../../src/scenario/federal/update_horario_funcionamento_federal/update_horario_funcionamento_federal_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Horario De Funcionamento Federal
Metadata    Test Suite Description        This test suite validates the PATCH update horario de funcionamento Federal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateHorarioDeFuncionamentoFederal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-143: Validate PATCH Update Horario De Funcionamento Federal - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update horario de funcionamento Federal endpoint with
    ...    HTTP 200 OK response.
    [Tags]    PATCH    UpdateHorarioDeFuncionamentoFederal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - UPDATE HORARIO DE FUNCIONAMENTO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-143: Validate PATCH Update Horario De Funcionamento Federal - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update horario de funcionamento Federal endpoint with
    ...    HTTP 404 NOT FOUND response.
    [Tags]    PATCH    UpdateHorarioDeFuncionamentoFederal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - UPDATE HORARIO DE FUNCIONAMENTO - PATCH    ${update_horario_funcionamento_federal_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-143: Validate PATCH Update Horario De Funcionamento Federal - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update horario de funcionamento Federal endpoint with
    ...    HTTP 400 BAD REQUEST response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateHorarioDeFuncionamentoFederal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    FEDERAL - UPDATE HORARIO DE FUNCIONAMENTO - PATCH    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
