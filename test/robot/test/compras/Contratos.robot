*** Settings ***
Resource    ../../src/scenario/compras/contratos_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Contratos
Metadata    Test Suite Description        Contratação, homologação, atas, contratos e aditivos (ADR-0213).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    Contratos    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-18
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Abertura Da Contratacao - HTTP 400 E 201
    [Documentation]    Dispensa sem fundamento dá 400; o pregão abre com os itens somados.
    [Tags]    Contratos    HTTP400    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONTRATOS    ABERTURA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Homologacao - HTTP 200
    [Documentation]    Acima da referência pede justificativa; gestor igual ao fiscal dá 422; homologa a ata.
    [Tags]    Contratos    HTTP422    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONTRATOS    HOMOLOGACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Aditivos Do Contrato - HTTP 201
    [Documentation]    Acréscimo até 25% do valor inicial; acima, 422.
    [Tags]    Contratos    HTTP201    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONTRATOS    ADITIVO_CONTRATO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Aditivos Da Ata - HTTP 422
    [Documentation]    A ata não aceita acréscimo e prorroga uma vez.
    [Tags]    Contratos    HTTP422    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CONTRATOS    ADITIVO_ATA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
