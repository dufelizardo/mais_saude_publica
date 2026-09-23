*** Settings ***
Resource    ../../../src/scenario/perfil_por_tipo_unidade/get_perfil_por_tipo_unidade_by_tipo/get_perfil_por_tipo_unidade_by_tipo_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Perfil Por Tipo Unidade By Tipo
Metadata    Test Suite Description        This test suite validates the GET Perfil Por Tipo Unidade by tipo endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    GetPerfilPorTipoUnidadeByTipo    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET Get Perfil Por Tipo Unidade By Tipo - HTTP 200 OK
    [Documentation]    Test case to validate the GET Perfil Por Tipo Unidade by tipo endpoint with HTTP 200 OK
    ...    response.
    [Tags]    GET    GetPerfilPorTipoUnidadeByTipo    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - GET BY TIPO - GET    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate GET Get Perfil Por Tipo Unidade By Tipo - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the GET Perfil Por Tipo Unidade by tipo endpoint with HTTP 404 NOT
    ...    FOUND response, when the tipo has no association configured.
    [Tags]    GET    GetPerfilPorTipoUnidadeByTipo    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PERFIL POR TIPO UNIDADE - GET BY TIPO - GET    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
