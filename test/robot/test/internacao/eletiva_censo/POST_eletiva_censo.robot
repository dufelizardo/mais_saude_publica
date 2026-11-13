*** Settings ***
Resource    ../../../src/scenario/internacao/eletiva_censo/eletiva_censo_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Internacao Eletiva E Censo Diario
Metadata    Test Suite Description        Internação eletiva com reserva de leito e censo diário formal (ADR-0185).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    InternacaoEletiva    CensoDiario    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-10
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST InternacaoEletiva - HTTP 201 CREATED E 409 CONFLICT
    [Documentation]    Eletiva agendada aparece na agenda; outra do mesmo paciente dá 409.
    [Tags]    POST    GET    InternacaoEletiva    HTTP201    HTTP409
    ELETIVA E CENSO    AGENDAR

CT-002 - Validate Reserva E Admissao - HTTP 200 OK
    [Documentation]    No dia, o leito é reservado e o paciente admitido; o leito fica ocupado.
    [Tags]    POST    InternacaoEletiva    HTTP200
    ELETIVA E CENSO    RESERVA_ADMISSAO

CT-003 - Validate POST Reserva - HTTP 422 (Fora Da Janela)
    [Documentation]    A reserva só abre dois dias antes da data prevista.
    [Tags]    POST    InternacaoEletiva    HTTP422
    ELETIVA E CENSO    RESERVA_422

CT-004 - Validate Remarcacao E Cancelamento - HTTP 400 E 200
    [Documentation]    Remarcar pede motivo; depois a eletiva é cancelada.
    [Tags]    POST    InternacaoEletiva    HTTP400    HTTP200
    ELETIVA E CENSO    REMARCAR_CANCELAR

CT-005 - Validate POST Falta - HTTP 422 E 200
    [Documentation]    A falta só é registrada a partir do dia previsto.
    [Tags]    POST    InternacaoEletiva    HTTP422    HTTP200
    ELETIVA E CENSO    FALTA

CT-006 - Validate Censo Diario - Previa, Fechamento E Reabertura
    [Documentation]    Prévia, fechamento (201), duplicado (409), reabertura sem motivo (400) e com motivo, nova versão; hoje não fecha (422).
    [Tags]    POST    GET    CensoDiario    HTTP200    HTTP201    HTTP400    HTTP409    HTTP422
    ELETIVA E CENSO    CENSO
