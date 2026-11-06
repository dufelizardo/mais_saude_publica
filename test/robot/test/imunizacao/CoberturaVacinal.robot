*** Settings ***
Resource    ../../src/scenario/imunizacao/cobertura_vacinal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - CoberturaVacinal
Metadata    Test Suite Description        This test suite validates the vaccination coverage, active search and campaign progress (ADR-0149) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               CoberturaVacinal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Cobertura Vacinal - HTTP 200 (Cobertura Vacinal - Unidade)
    [Documentation]    Cobertura Vacinal - Unidade.
    [Tags]    CoberturaVacinal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    200-unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Cobertura Vacinal - HTTP 200 (Cobertura Vacinal - Equipe Por Microarea)
    [Documentation]    Cobertura Vacinal - Equipe Por Microarea.
    [Tags]    CoberturaVacinal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    200-microarea
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Cobertura Vacinal - HTTP 200 (Cobertura Vacinal - Abandono)
    [Documentation]    Cobertura Vacinal - Abandono.
    [Tags]    CoberturaVacinal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    200-abandono
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Cobertura Vacinal - HTTP 400 (Cobertura Vacinal - Microarea Sem Equipe)
    [Documentation]    Cobertura Vacinal - Microarea Sem Equipe.
    [Tags]    CoberturaVacinal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    400-microarea
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Cobertura Vacinal - HTTP 400 (Cobertura Vacinal - Data Futura)
    [Documentation]    Cobertura Vacinal - Data Futura.
    [Tags]    CoberturaVacinal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    400-futuro
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Cobertura Vacinal - HTTP 200 (Busca Ativa - Microarea Com ACS)
    [Documentation]    Busca Ativa - Microarea Com ACS.
    [Tags]    CoberturaVacinal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    200-busca-ativa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Cobertura Vacinal - HTTP 400 (Busca Ativa - Equipe De Outra Unidade)
    [Documentation]    Busca Ativa - Equipe De Outra Unidade.
    [Tags]    CoberturaVacinal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    400-equipe
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Cobertura Vacinal - HTTP 201 (Campanha - Andamento Pela Dose)
    [Documentation]    Campanha - Andamento Pela Dose.
    [Tags]    CoberturaVacinal    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    201-campanha
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Cobertura Vacinal - HTTP 422 (Campanha - Outra Vacina)
    [Documentation]    Campanha - Outra Vacina.
    [Tags]    CoberturaVacinal    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    422-campanha
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Cobertura Vacinal - HTTP 200 (Sala De Situacao - Vacinacao)
    [Documentation]    Sala De Situacao - Vacinacao.
    [Tags]    CoberturaVacinal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    200-sala-situacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-011 - Validate Cobertura Vacinal - HTTP 400 (Calendario - Meta De Cobertura Sem Valor)
    [Documentation]    Calendario - Meta De Cobertura Sem Valor.
    [Tags]    CoberturaVacinal    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COBERTURA VACINAL    400-meta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
