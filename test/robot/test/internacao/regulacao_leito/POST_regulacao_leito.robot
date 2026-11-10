*** Settings ***
Resource    ../../../src/scenario/internacao/regulacao_leito/regulacao_leito_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Regulacao De Leitos
Metadata    Test Suite Description        Central de regulação de leitos entre unidades (ADR-0184).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    RegulacaoLeito    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-10
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST RegulacaoLeito - HTTP 201 CREATED E 409 CONFLICT
    [Documentation]    Pedido na fila; outro do mesmo paciente dá 409.
    [Tags]    POST    RegulacaoLeito    HTTP201    HTTP409
    REGULACAO DE LEITOS    PEDIDO

CT-002 - Validate Reserva E Admissao - HTTP 200 OK
    [Documentation]    Leito compatível de outra unidade, reservado e admitido.
    [Tags]    POST    GET    RegulacaoLeito    HTTP200
    REGULACAO DE LEITOS    RESERVA_ADMISSAO

CT-003 - Validate POST Reserva - HTTP 422 (Mesma Unidade)
    [Documentation]    Leito da própria origem não é destino regulado.
    [Tags]    POST    RegulacaoLeito    HTTP422
    REGULACAO DE LEITOS    RESERVA_422

CT-004 - Validate POST Reserva - HTTP 400 (Vaga Zero Sem Justificativa)
    [Documentation]    Vaga zero pede justificativa.
    [Tags]    POST    RegulacaoLeito    HTTP400
    REGULACAO DE LEITOS    VAGA_ZERO_400

CT-005 - Validate POST Recusa - HTTP 200 OK
    [Documentation]    Volta à fila e o leito fica livre.
    [Tags]    POST    RegulacaoLeito    HTTP200
    REGULACAO DE LEITOS    RECUSA

CT-006 - Validate POST Negacao - HTTP 200 OK
    [Documentation]    O regulador nega com motivo.
    [Tags]    POST    RegulacaoLeito    HTTP200
    REGULACAO DE LEITOS    NEGAR
