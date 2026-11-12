*** Settings ***
Resource    ../../src/scenario/rede/rede_tematica_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET/POST Redes Tematicas e Referencia
Metadata    Test Suite Description        Redes temáticas e referência estrutural da Gestão da Rede (ADR-0197).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    POST    RedeTematica    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Referencia E Autorizacao Fora Dela - HTTP 400 409 422 E 200
    [Documentation]    Mesma unidade, 400; repetida, 409; a sugestão; a autorização em outra executante marcada; o indicador; o painel da rede; o encerramento.
    [Tags]    GET    POST    RedeTematica    HTTP400    HTTP409    HTTP422    HTTP200
    REDES TEMATICAS    REFERENCIA
