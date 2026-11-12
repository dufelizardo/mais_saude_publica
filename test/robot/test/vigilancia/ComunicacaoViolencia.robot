*** Settings ***
Resource    ../../src/scenario/vigilancia/comunicacao_violencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Comunicação da Violência
Metadata    Test Suite Description        A comunicação obrigatória da violência notificada à rede intersetorial (ADR-0201).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    ComunicacaoViolencia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Mulher Adulta A Policia - HTTP 201
    [Documentation]    A violência contra a mulher adulta vai à autoridade policial; outro tipo, 422.
    [Tags]    POST    ComunicacaoViolencia    HTTP201    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COMUNICACAO DA VIOLENCIA    MULHER
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Crianca Ao Conselho Tutelar - HTTP 201
    [Documentation]    A violência contra a criança vai ao Conselho Tutelar.
    [Tags]    POST    ComunicacaoViolencia    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COMUNICACAO DA VIOLENCIA    CRIANCA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Comunicacoes Pendentes - HTTP 200
    [Documentation]    A pessoa idosa sem comunicação aparece nas pendentes.
    [Tags]    GET    ComunicacaoViolencia    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COMUNICACAO DA VIOLENCIA    PENDENTES
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Homem Adulto Sem Obrigacao - HTTP 422
    [Documentation]    Sem obrigação, a comunicação é recusada.
    [Tags]    POST    ComunicacaoViolencia    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    COMUNICACAO DA VIOLENCIA    HOMEM
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
