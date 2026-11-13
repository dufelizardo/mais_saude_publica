*** Settings ***
Resource    ../../src/scenario/transporte/frota_emergencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Frota nas Emergencias
Metadata    Test Suite Description        Pedido de veículo da emergência, cessão e devolução pela frota (ADR-0192).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    FrotaEmergencia    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-11
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Ceder E Devolver - HTTP 201 400 422 E 200
    [Documentation]    Cedido, o veículo fica em serviço; a devolução com a quilometragem que não volta o deixa disponível.
    [Tags]    POST    GET    FrotaEmergencia    HTTP201    HTTP400    HTTP422    HTTP200
    FROTA NAS EMERGENCIAS    CEDER_DEVOLVER

CT-002 - Validate Limites E Desmobilizacao - HTTP 409 422 E 200
    [Documentation]    Já cedido, 409; em manutenção ou acima da quantidade, 422; a desmobilização devolve.
    [Tags]    POST    GET    FrotaEmergencia    HTTP409    HTTP422    HTTP200
    FROTA NAS EMERGENCIAS    LIMITES_DESMOBILIZACAO
