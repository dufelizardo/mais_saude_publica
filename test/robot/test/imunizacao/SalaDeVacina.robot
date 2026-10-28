*** Settings ***
Resource    ../../src/scenario/imunizacao/sala_de_vacina_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - SalaDeVacina
Metadata    Test Suite Description        This test suite validates the vaccination room stock and cold chain (ADR-0148) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               SalaDeVacina    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-28
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Sala De Vacina - HTTP 201 (Sala De Vacina Entrada De Lote)
    [Documentation]    A batch enters the stock of the unit with the vaccination room.
    [Tags]    SalaDeVacina    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    201-entrada
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Sala De Vacina - HTTP 422 (Sala De Vacina Entrada Sem Sala)
    [Documentation]    A unit without the vaccination room capability does not control stock.
    [Tags]    SalaDeVacina    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    422-sem-sala
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Sala De Vacina - HTTP 201 (Sala De Vacina Dose Com Baixa)
    [Documentation]    The applied dose leaves the stock and opens the multidose vial.
    [Tags]    SalaDeVacina    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    201-baixa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Sala De Vacina - HTTP 400 (Sala De Vacina Dose Sem Lote Do Estoque)
    [Documentation]    In the unit with the vaccination room, the dose needs the stock batch.
    [Tags]    SalaDeVacina    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    400-sem-lote-estoque
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Sala De Vacina - HTTP 200 (Sala De Vacina Estoque)
    [Documentation]    The stock panel with the open vial.
    [Tags]    SalaDeVacina    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    200-estoque
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Sala De Vacina - HTTP 200 (Sala De Vacina Anulacao Estorna)
    [Documentation]    Voiding the dose returns it to the batch.
    [Tags]    SalaDeVacina    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    200-estorno
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Sala De Vacina - HTTP 422 (Sala De Vacina Perda Acima Do Saldo)
    [Documentation]    The physical loss does not exceed the balance.
    [Tags]    SalaDeVacina    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    422-perda
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Sala De Vacina - HTTP 422 (Sala De Vacina Excursao Bloqueia)
    [Documentation]    A temperature excursion blocks the chamber batch.
    [Tags]    SalaDeVacina    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    422-excursao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Sala De Vacina - HTTP 200 (Sala De Vacina Avaliacao Inutiliza)
    [Documentation]    The coordination discards the suspicious batch.
    [Tags]    SalaDeVacina    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    200-inutiliza
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Sala De Vacina - HTTP 200 (Sala De Vacina Cadeia De Frio)
    [Documentation]    The cold chain panel of the day.
    [Tags]    SalaDeVacina    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SALA DE VACINA    200-cadeia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
