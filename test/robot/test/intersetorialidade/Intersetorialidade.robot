*** Settings ***
Resource    ../../src/scenario/intersetorialidade/intersetorialidade_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Intersetorialidade
Metadata    Test Suite Description        This test suite validates the external institutions catalog, the intersectoral referral of the mental health case, the Mental Health indicators and the intersectoral network dashboard (ADRs 0154, 0155 and 0211) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Intersetorialidade    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      2026-11-18
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

CT-011 - Validate Intersetorialidade - HTTP 200 (Painel - Sem Retorno Por Instituicao)
    [Documentation]    Painel - Sem Retorno Por Instituicao.
    [Tags]    Intersetorialidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    200-painel
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-012 - Validate Intersetorialidade - HTTP 200 (Painel - Lista De Cobranca)
    [Documentation]    Painel - Lista De Cobranca.
    [Tags]    Intersetorialidade    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    200-cobranca
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-013 - Validate Intersetorialidade - HTTP 400 (Painel - Periodo Invertido)
    [Documentation]    Painel - Periodo Invertido.
    [Tags]    Intersetorialidade    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    400-periodo-invertido
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-014 - Validate Intersetorialidade - HTTP 404 (Painel - Instituicao Inexistente)
    [Documentation]    Painel - Instituicao Inexistente.
    [Tags]    Intersetorialidade    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INTERSETORIALIDADE    404-instituicao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
