*** Settings ***
Resource    ../../../src/scenario/internacao/prescricao_hospitalar/prescricao_hospitalar_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Prescricao Hospitalar
Metadata    Test Suite Description        Prescrição hospitalar de 24 horas, aprazamento e checagem pela enfermagem (ADR-0187).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    PrescricaoHospitalar    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-10
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST PrescricaoHospitalar - HTTP 201 400 E 409
    [Documentation]    Prescrição com horários aprazados; sem itens dá 400; outra no mesmo início dá 409.
    [Tags]    POST    GET    PrescricaoHospitalar    HTTP201    HTTP400    HTTP409
    PRESCRICAO HOSPITALAR    PRESCREVER

CT-002 - Validate Checagem Com Baixa Do Lote - HTTP 400 200 E 422
    [Documentation]    Não feito pede motivo; feito com lote baixa o saldo; checar de novo dá 422.
    [Tags]    POST    PrescricaoHospitalar    HTTP400    HTTP200    HTTP422
    PRESCRICAO HOSPITALAR    CHECAR_LOTE

CT-003 - Validate Suspensao E Renovacao - HTTP 200 E 201
    [Documentation]    O item suspenso cancela os horários e não é copiado na renovação.
    [Tags]    POST    PrescricaoHospitalar    HTTP200    HTTP201
    PRESCRICAO HOSPITALAR    SUSPENDER_RENOVAR

CT-004 - Validate Reaprazamento E Posto - HTTP 422 E 200
    [Documentation]    Reaprazar fora da vigência dá 422; dentro, com motivo, muda o horário; o posto lista os horários.
    [Tags]    POST    GET    PrescricaoHospitalar    HTTP422    HTTP200
    PRESCRICAO HOSPITALAR    REAPRAZAR_POSTO

CT-005 - Validate Dieta E Retificacao - HTTP 400 E 200
    [Documentation]    A dieta pede a aceitação; a checagem é corrigida por retificação com motivo.
    [Tags]    POST    PrescricaoHospitalar    HTTP400    HTTP200
    PRESCRICAO HOSPITALAR    DIETA_RETIFICACAO
