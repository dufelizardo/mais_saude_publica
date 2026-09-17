*** Settings ***
Resource    ../../../src/scenario/unidade_saude/update_supervisao_regional_unidade_saude/update_supervisao_regional_unidade_saude_scenario.resource
Resource    ../../../src/resource/config/driven/data_driven.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Update Supervisao Regional Unidade Saude
Metadata    Test Suite Description        This test suite validates the PATCH update supervisao regional Unidade de Saúde endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    UpdateSupervisaoRegionalUnidadeSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Update Supervisao Regional Unidade Saude - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH update supervisao regional Unidade de Saúde endpoint
    ...    with HTTP 200 OK response.
    [Tags]    PATCH    UpdateSupervisaoRegionalUnidadeSaude    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH    ${EMPTY}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Update Supervisao Regional Unidade Saude - HTTP 404 NOT FOUND (Unidade Inexistente)
    [Documentation]    Test case to validate the PATCH update supervisao regional Unidade de Saúde endpoint
    ...    with HTTP 404 NOT FOUND response, when the Unidade de Saúde nome in the URL doesn't exist.
    [Tags]    PATCH    UpdateSupervisaoRegionalUnidadeSaude    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH    ${update_supervisao_regional_unidade_saude_nonexistent_nome_404}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH Update Supervisao Regional Unidade Saude - HTTP 404 NOT FOUND (Supervisao Regional Inexistente)
    [Documentation]    Test case to validate the PATCH update supervisao regional Unidade de Saúde endpoint
    ...    with HTTP 404 NOT FOUND response, when the supervisaoRegional value in the body doesn't exist
    ...    anywhere.
    [Tags]    PATCH    UpdateSupervisaoRegionalUnidadeSaude    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH - Supervisao Regional Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate PATCH Update Supervisao Regional Unidade Saude - HTTP 422 UNPROCESSABLE ENTITY (Supervisao Regional Nivel Errado)
    [Documentation]    Test case to validate the PATCH update supervisao regional Unidade de Saúde endpoint
    ...    with HTTP 422 UNPROCESSABLE ENTITY response, when supervisaoRegional points at a unit of the wrong
    ...    hierarchy level (Municipal instead of Regional).
    [Tags]    PATCH    UpdateSupervisaoRegionalUnidadeSaude    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH    ${EMPTY}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate PATCH Update Supervisao Regional Unidade Saude - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the PATCH update supervisao regional Unidade de Saúde endpoint
    ...    with HTTP 400 BAD REQUEST response, when the supervisaoRegional value is left blank.
    [Tags]    PATCH    UpdateSupervisaoRegionalUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH - Campos Obrigatorios Em Branco
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate PATCH Update Supervisao Regional Unidade Saude - HTTP 400 BAD REQUEST (Corpo Ilegivel)
    [Documentation]    Test case to validate the PATCH update supervisao regional Unidade de Saúde endpoint
    ...    with HTTP 400 BAD REQUEST response, when the request body is syntactically broken JSON (AQUAQE-215).
    [Tags]    PATCH    UpdateSupervisaoRegionalUnidadeSaude    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UNIDADE SAUDE - UPDATE SUPERVISAO REGIONAL - PATCH - Corpo Ilegivel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
