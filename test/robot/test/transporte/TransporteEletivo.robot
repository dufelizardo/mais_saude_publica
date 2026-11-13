*** Settings ***
Resource    ../../src/scenario/transporte/transporte_eletivo_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Transporte eletivo e TFD
Metadata    Test Suite Description        Solicitação de transporte eletivo, TFD com a comissão, recorrência e viagens (ADR-0190).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    PUT    TransporteEletivo    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Solicitacao E TFD - HTTP 400 422 E 201
    [Documentation]    Maca sem acompanhante e TFD sem laudo dão 400; o TFD aguarda a comissão e se autoriza uma vez.
    [Tags]    POST    GET    TransporteEletivo    HTTP400    HTTP422    HTTP201
    TRANSPORTE ELETIVO    SOLICITACAO_TFD

CT-002 - Validate Recorrencia - HTTP 400 E 201
    [Documentation]    Consulta não é recorrente; a hemodiálise gera as sessões e o cancelamento leva as próximas.
    [Tags]    POST    TransporteEletivo    HTTP400    HTTP201
    TRANSPORTE ELETIVO    RECORRENCIA

CT-003 - Validate Viagem - HTTP 409 422 E 200
    [Documentation]    Programa, sai, registra a falta e chega com a quilometragem; a mesma solicitação de novo dá 409.
    [Tags]    POST    GET    TransporteEletivo    HTTP409    HTTP422    HTTP200
    TRANSPORTE ELETIVO    VIAGEM
