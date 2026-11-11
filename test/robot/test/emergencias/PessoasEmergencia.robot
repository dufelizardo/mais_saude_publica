*** Settings ***
Resource    ../../src/scenario/emergencias/pessoas_emergencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Pessoas na Emergencia
Metadata    Test Suite Description        Vulneráveis da área afetada, pacientes deslocados, abrigos e encaminhamento (ADR-0193).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    PessoasEmergencia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Vulneraveis Da Area - HTTP 200
    [Documentation]    A criança da microárea afetada vem com o marcador e o ACS; o filtro e o CSV.
    [Tags]    GET    PessoasEmergencia    HTTP200
    PESSOAS NA EMERGENCIA    VULNERAVEIS

CT-002 - Validate Deslocado E Encaminhamento - HTTP 400 409 422 201 E 200
    [Documentation]    Abrigo sem instituição nem nome, 400; duas vezes, 409; realocação, aviso do prontuário, encaminhamento e retorno.
    [Tags]    POST    GET    PessoasEmergencia    HTTP400    HTTP409    HTTP422    HTTP201    HTTP200
    PESSOAS NA EMERGENCIA    DESLOCADO
