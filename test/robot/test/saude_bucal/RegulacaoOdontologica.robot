*** Settings ***
Resource    ../../src/scenario/saude_bucal/regulacao_odontologica_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Regulacao Odontologica
Metadata    Test Suite Description        This test suite validates the dental referral through the Regulation (ADR-0138) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               RegulacaoOdontologica    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-22
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Regulacao Odontologica - HTTP 200 (Regulacao Odontologica Catalogo)
    [Documentation]    The regulated catalog brings endodontics, which requires the CEO capacity.
    [Tags]    RegulacaoOdontologica    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO ODONTOLOGICA    200-catalogo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Regulacao Odontologica - HTTP 422 (Regulacao Odontologica Executante Sem Capacidade)
    [Documentation]    A unit without the endodontics capacity does not receive it.
    [Tags]    RegulacaoOdontologica    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO ODONTOLOGICA    422-executante
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Regulacao Odontologica - HTTP 201 (Regulacao Odontologica Encaminhar O Item)
    [Documentation]    The request from the plan item leaves it forwarded.
    [Tags]    RegulacaoOdontologica    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO ODONTOLOGICA    201-encaminhar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Regulacao Odontologica - HTTP 200 (Regulacao Odontologica Negativa Devolve O Item)
    [Documentation]    The denial brings the item back to planned.
    [Tags]    RegulacaoOdontologica    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO ODONTOLOGICA    200-negativa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Regulacao Odontologica - HTTP 422 (Regulacao Odontologica Item Ja Encaminhado)
    [Documentation]    A forwarded item is not forwarded again.
    [Tags]    RegulacaoOdontologica    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    REGULACAO ODONTOLOGICA    422-encaminhado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
