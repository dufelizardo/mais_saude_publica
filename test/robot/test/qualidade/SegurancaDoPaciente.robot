*** Settings ***
Resource    ../../src/scenario/qualidade/seguranca_do_paciente_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Segurança do Paciente
Metadata    Test Suite Description        Notificação de incidente, análise do núcleo, Notivisa e painel (ADR-0203).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    SegurancaDoPaciente    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Notificacao Anonima - HTTP 201
    [Documentation]    A anônima dá o protocolo; ocorrência futura, 400.
    [Tags]    POST    SegurancaDoPaciente    HTTP201    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SEGURANCA DO PACIENTE    ANONIMA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Analise Com Causa Raiz - HTTP 200
    [Documentation]    Dano grave pede a causa raiz para concluir.
    [Tags]    POST    SegurancaDoPaciente    HTTP200    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SEGURANCA DO PACIENTE    CAUSA_RAIZ
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Notivisa Do Obito - HTTP 200
    [Documentation]    Óbito em 72 horas; registrado uma vez (409).
    [Tags]    POST    SegurancaDoPaciente    HTTP200    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SEGURANCA DO PACIENTE    NOTIVISA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Painel - HTTP 200
    [Documentation]    O painel conta as notificações.
    [Tags]    GET    SegurancaDoPaciente    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SEGURANCA DO PACIENTE    PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
