*** Settings ***
Resource    ../../../src/scenario/estadual/update_horario_atendimento_estadual/update_horario_atendimento_estadual_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Horario De Atendimento Estadual
Metadata    Test Suite Description        This test suite validates the PATCH update horario de atendimento Estadual endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateHorarioDeAtendimentoEstadual    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-07
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - AQUAQE-144: Validate PATCH Update Horario De Atendimento Estadual - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update horario de atendimento Estadual endpoint with
    ...    HTTP 200 OK response.
    [Tags]    PATCH    UpdateHorarioDeAtendimentoEstadual    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE HORARIO DE ATENDIMENTO - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - AQUAQE-144: Validate PATCH Update Horario De Atendimento Estadual - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH update horario de atendimento Estadual endpoint with
    ...    HTTP 404 NOT FOUND response.
    [Tags]    PATCH    UpdateHorarioDeAtendimentoEstadual    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE HORARIO DE ATENDIMENTO - PATCH    ${update_horario_atendimento_estadual_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - AQUAQE-144: Validate PATCH Update Horario De Atendimento Estadual - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update horario de atendimento Estadual endpoint with
    ...    HTTP 400 BAD REQUEST response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateHorarioDeAtendimentoEstadual    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ESTADUAL - UPDATE HORARIO DE ATENDIMENTO - PATCH    ${EMPTY}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
