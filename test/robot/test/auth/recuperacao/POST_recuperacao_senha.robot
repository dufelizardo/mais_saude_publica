*** Settings ***
Resource    ../../../src/scenario/auth/recuperacao/recuperacao_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Recuperacao De Senha
Metadata    Test Suite Description        Validates POST /api/v1/auth/senha/recuperacao and /redefinicao (ADR-0081).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    RecuperacaoDeSenha    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Recuperacao - HTTP 400 BAD REQUEST
    [Documentation]    CPF em branco.
    [Tags]    POST    RecuperacaoDeSenha    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - SENHA RECUPERACAO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Recuperacao - HTTP 422 UNPROCESSABLE ENTITY
    [Documentation]    Indisponível sem login/SMTP.
    [Tags]    POST    RecuperacaoDeSenha    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - SENHA RECUPERACAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Redefinicao - HTTP 400 BAD REQUEST
    [Documentation]    Senha curta.
    [Tags]    POST    RecuperacaoDeSenha    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - SENHA REDEFINICAO - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST Redefinicao - HTTP 422 UNPROCESSABLE ENTITY
    [Documentation]    Indisponível sem login/SMTP.
    [Tags]    POST    RecuperacaoDeSenha    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    AUTH - SENHA REDEFINICAO - POST    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
