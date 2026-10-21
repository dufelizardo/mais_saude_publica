*** Settings ***
Resource    ../../src/scenario/saude_bucal/procedimento_odontologico_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Procedimento Odontologico
Metadata    Test Suite Description        This test suite validates the general procedure catalog and the dental procedure (ADR-0136) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               ProcedimentoOdontologico    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-21
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Procedimento Odontologico - HTTP 200 (Procedimento Odontologico Catalogo)
    [Documentation]    The catalog brings the composite resin restoration, which records Restored.
    [Tags]    ProcedimentoOdontologico    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    200-catalogo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Procedimento Odontologico - HTTP 201 (Procedimento Odontologico Item Do Catalogo)
    [Documentation]    Registers a general catalog item with a SIGTAP code.
    [Tags]    ProcedimentoOdontologico    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    201-item
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Procedimento Odontologico - HTTP 422 (Procedimento Odontologico Item Geral Com Dente)
    [Documentation]    Tooth and face are for a dental procedure only.
    [Tags]    ProcedimentoOdontologico    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    422-item
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Procedimento Odontologico - HTTP 409 (Procedimento Odontologico Codigo Repetido)
    [Documentation]    The catalog code is unique.
    [Tags]    ProcedimentoOdontologico    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    409-item
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Procedimento Odontologico - HTTP 201 (Procedimento Odontologico Restauracao Grava O Odontograma)
    [Documentation]    A realized restoration on 36 occlusal records Restored, with the procedure as origin.
    [Tags]    ProcedimentoOdontologico    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    201-restauracao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Procedimento Odontologico - HTTP 400 (Procedimento Odontologico Sem Face)
    [Documentation]    The restoration needs the face.
    [Tags]    ProcedimentoOdontologico    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    400-face
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Procedimento Odontologico - HTTP 422 (Procedimento Odontologico Face Que O Dente Nao Tem)
    [Documentation]    An anterior tooth has no occlusal face.
    [Tags]    ProcedimentoOdontologico    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    422-face
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Procedimento Odontologico - HTTP 422 (Procedimento Odontologico Fora Do Atendimento Odontologico)
    [Documentation]    A dental procedure only in a dental-care visit.
    [Tags]    ProcedimentoOdontologico    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    422-atendimento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Procedimento Odontologico - HTTP 201 (Procedimento Odontologico Retificacao Anula E Regrava)
    [Documentation]    Rectifying the tooth of an extraction voids the 48 record and records 47.
    [Tags]    ProcedimentoOdontologico    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    201-retificacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Procedimento Odontologico - HTTP 400 (Procedimento Odontologico Sem Consulta Nem Atendimento)
    [Documentation]    The procedure needs the consultation or the visit.
    [Tags]    ProcedimentoOdontologico    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PROCEDIMENTO ODONTOLOGICO    400-atendimento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
