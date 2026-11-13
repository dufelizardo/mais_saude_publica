*** Settings ***
Resource    ../../src/scenario/comunicacao/educacao_em_saude_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PUT/POST Educacao em Saude
Metadata    Test Suite Description        O detalhe educativo da ação coletiva de Programas (ADR-0195).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PUT    POST    EducacaoEmSaude    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Detalhe Educativo Da Acao - HTTP 400 422 E 200
    [Documentation]    Sem tema, 400; com o conteúdo aprovado e a cartilha, 200; avaliar a prevista, 422; o uso conta a ação.
    [Tags]    PUT    POST    EducacaoEmSaude    HTTP400    HTTP422    HTTP200
    EDUCACAO EM SAUDE    DETALHE
