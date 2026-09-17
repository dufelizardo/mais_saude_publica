*** Settings ***
Resource    ../../../src/scenario/unidade_saude/update_horario_funcionamento_unidade_saude/update_horario_funcionamento_unidade_saude_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Horario De Funcionamento Unidade Saude
Metadata    Test Suite Description        This test suite validates the PATCH update horario de funcionamento Unidade de Saúde endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateHorarioDeFuncionamentoUnidadeSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Horario De Funcionamento Unidade Saude - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update horario de funcionamento Unidade de Saúde
    ...    endpoint with HTTP 200 OK response.
    [Tags]    PATCH    UpdateHorarioDeFuncionamentoUnidadeSaude    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE HORARIO DE FUNCIONAMENTO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Horario De Funcionamento Unidade Saude - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update horario de funcionamento Unidade de Saúde
    ...    endpoint with HTTP 404 NOT FOUND response.
    [Tags]    PATCH    UpdateHorarioDeFuncionamentoUnidadeSaude    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE HORARIO DE FUNCIONAMENTO - PATCH    ${update_horario_funcionamento_unidade_saude_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH Update Horario De Funcionamento Unidade Saude - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update horario de funcionamento Unidade de Saúde
    ...    endpoint with HTTP 400 BAD REQUEST response, when the request body is syntactically broken JSON
    ...    (AQUAQE-215).
    [Tags]    PATCH    UpdateHorarioDeFuncionamentoUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE HORARIO DE FUNCIONAMENTO - PATCH    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
