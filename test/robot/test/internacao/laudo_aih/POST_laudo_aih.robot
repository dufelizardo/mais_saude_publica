*** Settings ***
Resource    ../../../src/scenario/internacao/laudo_aih/laudo_aih_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET/PUT Laudo De AIH
Metadata    Test Suite Description        Laudo para solicitação de AIH, autorização interna com o número e faixas de numeração (ADR-0186).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    PUT    LaudoAih    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-10
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST LaudoAih - HTTP 201 CREATED E 409 CONFLICT
    [Documentation]    Laudo solicitado entra na fila; outro da mesma internação dá 409.
    [Tags]    POST    GET    LaudoAih    HTTP201    HTTP409
    LAUDO DE AIH    SOLICITAR

CT-002 - Validate Devolucao E Correcao - HTTP 400 E 200
    [Documentation]    Devolver pede motivo; o laudo corrigido volta para a fila.
    [Tags]    POST    PUT    LaudoAih    HTTP400    HTTP200
    LAUDO DE AIH    DEVOLVER_CORRIGIR

CT-003 - Validate Autorizacao Com Numero - HTTP 422 E 200
    [Documentation]    Quem solicitou não autoriza; outro médico autoriza com número válido, copiado para a internação; PDF.
    [Tags]    POST    GET    LaudoAih    HTTP422    HTTP200
    LAUDO DE AIH    AUTORIZAR

CT-004 - Validate Negacao E Cancelamento - HTTP 200 E 422
    [Documentation]    Negar com motivo; o negado não é cancelado; o solicitado é cancelado com motivo.
    [Tags]    POST    LaudoAih    HTTP200    HTTP422
    LAUDO DE AIH    NEGAR_CANCELAR

CT-005 - Validate Faixa De Numeracao - HTTP 400 E 409
    [Documentation]    Faixa invertida dá 400; sobreposta dá 409.
    [Tags]    POST    GET    LaudoAih    HTTP400    HTTP409
    LAUDO DE AIH    FAIXA
