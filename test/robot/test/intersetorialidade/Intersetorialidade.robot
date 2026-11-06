*** Settings ***
Resource    ../../src/scenario/intersetorialidade/intersetorialidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Intersetorialidade
Metadata    Test Suite Description        This test suite validates the external institutions catalog, the intersectoral referral of the mental health case and the Mental Health indicators (ADRs 0154 and 0155) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Intersetorialidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Intersetorialidade - HTTP 201 (Instituicao - Cadastro)
    [Documentation]    Instituicao - Cadastro.
    [Tags]    Intersetorialidade    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    201-instituicao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Intersetorialidade - HTTP 409 (Instituicao - Nome Repetido)
    [Documentation]    Instituicao - Nome Repetido.
    [Tags]    Intersetorialidade    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    409-nome-repetido
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Intersetorialidade - HTTP 422 (Instituicao - Tipo De Outro Setor)
    [Documentation]    Instituicao - Tipo De Outro Setor.
    [Tags]    Intersetorialidade    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    422-tipo-de-outro-setor
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Intersetorialidade - HTTP 200 (Instituicao - Inativar)
    [Documentation]    Instituicao - Inativar.
    [Tags]    Intersetorialidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    200-inativar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Intersetorialidade - HTTP 201 (Encaminhamento - Envio)
    [Documentation]    Encaminhamento - Envio.
    [Tags]    Intersetorialidade    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    201-encaminhamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Intersetorialidade - HTTP 409 (Encaminhamento - Em Aberto)
    [Documentation]    Encaminhamento - Em Aberto.
    [Tags]    Intersetorialidade    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    409-em-aberto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Intersetorialidade - HTTP 200 (Encaminhamento - Retorno)
    [Documentation]    Encaminhamento - Retorno.
    [Tags]    Intersetorialidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    200-retorno
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Intersetorialidade - HTTP 400 (Encaminhamento - Retorno Sem Texto)
    [Documentation]    Encaminhamento - Retorno Sem Texto.
    [Tags]    Intersetorialidade    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    400-retorno-sem-texto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Intersetorialidade - HTTP 200 (Encaminhamento - Sem Retorno No Prazo)
    [Documentation]    Encaminhamento - Sem Retorno No Prazo.
    [Tags]    Intersetorialidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    200-sem-retorno
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Intersetorialidade - HTTP 200 (Indicadores - Saude Mental)
    [Documentation]    Indicadores - Saude Mental.
    [Tags]    Intersetorialidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    200-indicadores
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
