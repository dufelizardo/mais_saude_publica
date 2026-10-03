*** Settings ***
Resource    ../../../src/scenario/procedimento_regulado/create_procedimento_regulado/create_procedimento_regulado_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST CreateProcedimentoRegulado
Metadata    Test Suite Description        This test suite validates the POST /api/v1/procedimento-regulado/ endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreateProcedimentoRegulado    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST CreateProcedimentoRegulado - HTTP 201 CREATED
    [Documentation]    Cadastro no catálogo, que aparece entre os ativos.
    [Tags]    POST    CreateProcedimentoRegulado    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO REGULADO - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST CreateProcedimentoRegulado - HTTP 409 CONFLICT (Nome Repetido)
    [Documentation]    Nome repetido, ignorando maiúsculas.
    [Tags]    POST    CreateProcedimentoRegulado    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO REGULADO - CREATE - POST    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST CreateProcedimentoRegulado - HTTP 400 BAD REQUEST (Sem Tipo)
    [Documentation]    Payload sem tipo.
    [Tags]    POST    CreateProcedimentoRegulado    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO REGULADO - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
