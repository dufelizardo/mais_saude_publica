*** Settings ***
Resource    ../../src/scenario/territorio/cadastro_territorial_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Cadastro Territorial
Metadata    Test Suite Description        This test suite validates the household and family registry (ADR-0127) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               CadastroTerritorial    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Cadastro Territorial - HTTP 201 (Imovel)
    [Documentation]    Imóvel achado pelo ponto, com schema.
    [Tags]    CadastroTerritorial    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    201-imovel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Cadastro Territorial - HTTP 422 (Ponto Fora Da Microarea)
    [Documentation]    Ponto fora da microárea.
    [Tags]    CadastroTerritorial    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    422-ponto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Cadastro Territorial - HTTP 422 (Area Sem Equipe)
    [Documentation]    Área sem equipe responsável.
    [Tags]    CadastroTerritorial    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    422-equipe
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Cadastro Territorial - HTTP 422 (Domicilio Em Comercio)
    [Documentation]    Domicílio só em imóvel de moradia.
    [Tags]    CadastroTerritorial    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    422-domicilio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Cadastro Territorial - HTTP 201 (Familia)
    [Documentation]    Família com responsável e filho; pessoa em outra família, 409.
    [Tags]    CadastroTerritorial    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    201-familia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Cadastro Territorial - HTTP 422 (Responsavel Sai Por Ultimo)
    [Documentation]    O responsável sai por último.
    [Tags]    CadastroTerritorial    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    422-responsavel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Cadastro Territorial - HTTP 200 (Mudanca)
    [Documentation]    Mudança com histórico de moradia.
    [Tags]    CadastroTerritorial    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    200-mudanca
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Cadastro Territorial - HTTP 404 (Imovel Inexistente)
    [Documentation]    Imóvel inexistente.
    [Tags]    CadastroTerritorial    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CADASTRO TERRITORIAL    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
