*** Settings ***
Resource    ../../src/scenario/vigilancia/vigilancia_contatos_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Vigilancia Contatos
Metadata    Test Suite Description        This test suite validates the contacts, the place of the case and the map of cases (ADR-0130) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               VigilanciaContatos    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Vigilancia Contatos - HTTP 201 (Contato Registro)
    [Documentation]    A contact without registration, with the monitoring period of measles.
    [Tags]    VigilanciaContatos    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    201-contato
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Vigilancia Contatos - HTTP 400 (Contato Sem Nome)
    [Documentation]    Without a registered patient, the contact needs a name.
    [Tags]    VigilanciaContatos    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    400-contato
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Vigilancia Contatos - HTTP 422 (Contato Proprio Caso)
    [Documentation]    The case cannot be its own contact.
    [Tags]    VigilanciaContatos    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    422-contato
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Vigilancia Contatos - HTTP 201 (Contato Vira Caso)
    [Documentation]    Notifying the contact links the new case to the index case; twice is refused.
    [Tags]    VigilanciaContatos    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    201-vira-caso
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Vigilancia Contatos - HTTP 404 (Contato Inexistente)
    [Documentation]    Updating a contact that does not exist.
    [Tags]    VigilanciaContatos    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    404-contato
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Vigilancia Contatos - HTTP 200 (Local Pelo Domicilio)
    [Documentation]    Without a marked point, the case takes the family's home and its micro-area.
    [Tags]    VigilanciaContatos    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    200-local-domicilio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Vigilancia Contatos - HTTP 200 (Local Marcado)
    [Documentation]    The investigator marks the point; the micro-area comes from the map.
    [Tags]    VigilanciaContatos    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    200-local-marcado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Vigilancia Contatos - HTTP 400 (Local Sem Longitude)
    [Documentation]    The point needs latitude and longitude.
    [Tags]    VigilanciaContatos    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    400-local
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Vigilancia Contatos - HTTP 200 (Mapa Casos)
    [Documentation]    The map brings the case, the curve and, for who investigates, the points.
    [Tags]    VigilanciaContatos    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA CONTATOS    200-mapa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
