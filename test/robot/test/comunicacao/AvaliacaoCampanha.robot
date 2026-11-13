*** Settings ***
Resource    ../../src/scenario/comunicacao/avaliacao_campanha_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET/POST Avaliacao da Campanha
Metadata    Test Suite Description        Metas, apuração e avaliação da campanha de comunicação (ADR-0196).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    POST    AvaliacaoCampanha    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Avaliacao Da Campanha - HTTP 400 422 E 200
    [Documentation]    Meta zero, 400; avaliar a campanha em andamento, 422; encerrada sem conclusões, 400; com elas, 200; o relatório traz a campanha.
    [Tags]    GET    POST    AvaliacaoCampanha    HTTP400    HTTP422    HTTP200
    AVALIACAO DA CAMPANHA    REGISTRO
