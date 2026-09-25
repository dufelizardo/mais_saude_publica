*** Settings ***
Resource    ../../../src/scenario/necessidade_de_pessoal/vincular_vaga_necessidade_de_pessoal/vincular_vaga_necessidade_de_pessoal_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PATCH Vincular Vaga Necessidade De Pessoal
Metadata    Test Suite Description        This test suite validates the PATCH vincular-vaga Necessidade De Pessoal endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PATCH    VincularVagaNecessidadeDePessoal    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-23
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PATCH Vincular Vaga Necessidade De Pessoal - HTTP 200 OK
    [Documentation]    Test case to validate the PATCH vincular-vaga Necessidade De Pessoal endpoint with HTTP
    ...    200 OK response.
    [Tags]    PATCH    VincularVagaNecessidadeDePessoal    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - VINCULAR VAGA - PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate PATCH Vincular Vaga Necessidade De Pessoal - HTTP 404 NOT FOUND
    [Documentation]    Test case to validate the PATCH vincular-vaga Necessidade De Pessoal endpoint with HTTP
    ...    404 NOT FOUND response, when vagaId points at a Vaga that doesn't exist.
    [Tags]    PATCH    VincularVagaNecessidadeDePessoal    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - VINCULAR VAGA - PATCH    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate PATCH Vincular Vaga Necessidade De Pessoal - HTTP 409 CONFLICT
    [Documentation]    Test case to validate the PATCH vincular-vaga Necessidade De Pessoal endpoint with HTTP
    ...    409 CONFLICT response, when the necessidade já tem vaga associada.
    [Tags]    PATCH    VincularVagaNecessidadeDePessoal    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    NECESSIDADE DE PESSOAL - VINCULAR VAGA - PATCH    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
