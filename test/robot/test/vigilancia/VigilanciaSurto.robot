*** Settings ***
Resource    ../../src/scenario/vigilancia/vigilancia_surto_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Vigilancia Surto
Metadata    Test Suite Description        This test suite validates the outbreaks and the control measures (ADR-0131) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               VigilanciaSurto    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Vigilancia Surto - HTTP 201 (Surto Abertura)
    [Documentation]    Opens an institutional outbreak with two measles cases.
    [Tags]    VigilanciaSurto    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    201-surto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Vigilancia Surto - HTTP 400 (Surto Institucional Sem Estabelecimento)
    [Documentation]    The institutional outbreak needs the establishment.
    [Tags]    VigilanciaSurto    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    400-institucional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Vigilancia Surto - HTTP 409 (Surto Caso Em Outro Surto)
    [Documentation]    A case belongs to one outbreak at a time.
    [Tags]    VigilanciaSurto    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    409-outro-surto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Vigilancia Surto - HTTP 422 (Surto Agravo Diferente)
    [Documentation]    The outbreak only takes cases of its agravo.
    [Tags]    VigilanciaSurto    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    422-agravo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Vigilancia Surto - HTTP 200 (Surto Retirar Caso)
    [Documentation]    A case leaves the outbreak; the notification stays.
    [Tags]    VigilanciaSurto    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    200-retirar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Vigilancia Surto - HTTP 200 (Surto Ciclo E Encerramento)
    [Documentation]    Close needs confirmed or discarded, the conclusion and no open measure.
    [Tags]    VigilanciaSurto    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    200-ciclo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Vigilancia Surto - HTTP 400 (Medida Concluir Sem Resultado)
    [Documentation]    Concluding a measure needs the result.
    [Tags]    VigilanciaSurto    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    400-concluir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Vigilancia Surto - HTTP 201 (Medida Do Caso)
    [Documentation]    A control measure of a single case, without an outbreak.
    [Tags]    VigilanciaSurto    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    201-medida-caso
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Vigilancia Surto - HTTP 200 (Surtos Lista)
    [Documentation]    The list brings the outbreak and the indicators.
    [Tags]    VigilanciaSurto    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    200-surtos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Vigilancia Surto - HTTP 200 (Surto Mapa)
    [Documentation]    The map of the outbreak brings only its cases.
    [Tags]    VigilanciaSurto    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    200-mapa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-011 - Validate Vigilancia Surto - HTTP 404 (Surto Inexistente)
    [Documentation]    Reading an outbreak that does not exist.
    [Tags]    VigilanciaSurto    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SURTO    404-surto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
