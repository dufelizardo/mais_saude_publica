*** Settings ***
Resource    ../../../src/scenario/necessidade_de_pessoal/create_necessidade_de_pessoal/create_necessidade_de_pessoal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Necessidade De Pessoal
Metadata    Test Suite Description        This test suite validates the POST create Necessidade De Pessoal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateNecessidadeDePessoal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Necessidade De Pessoal - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Necessidade De Pessoal endpoint with HTTP 201
    ...    CREATED response.
    [Tags]    POST    CreateNecessidadeDePessoal    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Necessidade De Pessoal - HTTP 404 NOT FOUND (Unidade Inexistente)
    [Documentation]    Test case to validate the POST create Necessidade De Pessoal endpoint with HTTP 404 NOT
    ...    FOUND response, when unidadeId points at a Unidade de Saúde that doesn't exist.
    [Tags]    POST    CreateNecessidadeDePessoal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - CREATE - POST    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Necessidade De Pessoal - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Necessidade De Pessoal endpoint with HTTP 400 BAD
    ...    REQUEST response, when required fields are omitted.
    [Tags]    POST    CreateNecessidadeDePessoal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Create Necessidade De Pessoal - HTTP 404 NOT FOUND (Cargo Inexistente)
    [Documentation]    Test case to validate the POST create Necessidade De Pessoal endpoint with HTTP 404 NOT
    ...    FOUND response, when cargoId points at a Cargo that doesn't exist.
    [Tags]    POST    CreateNecessidadeDePessoal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - CREATE - POST - Cargo Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate POST Create Necessidade De Pessoal - HTTP 404 NOT FOUND (Setor Inexistente)
    [Documentation]    Test case to validate the POST create Necessidade De Pessoal endpoint with HTTP 404 NOT
    ...    FOUND response, when setorId points at a Setor that doesn't exist.
    [Tags]    POST    CreateNecessidadeDePessoal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - CREATE - POST - Setor Inexistente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
