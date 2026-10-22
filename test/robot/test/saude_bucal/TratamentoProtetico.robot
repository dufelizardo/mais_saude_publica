*** Settings ***
Resource    ../../src/scenario/saude_bucal/tratamento_protetico_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Tratamento Protetico
Metadata    Test Suite Description        This test suite validates the prosthetic treatment (ADR-0139) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               TratamentoProtetico    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-22
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Tratamento Protetico - HTTP 201 (Tratamento Protetico Abrir)
    [Documentation]    Opens an upper complete denture; the next stage is the impression.
    [Tags]    TratamentoProtetico    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRATAMENTO PROTETICO    201-abrir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Tratamento Protetico - HTTP 400 (Tratamento Protetico Sem Arcada)
    [Documentation]    The complete denture needs the arch.
    [Tags]    TratamentoProtetico    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRATAMENTO PROTETICO    400-arcada
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Tratamento Protetico - HTTP 422 (Tratamento Protetico Fora Da Ordem)
    [Documentation]    The try-in comes after the impression.
    [Tags]    TratamentoProtetico    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRATAMENTO PROTETICO    422-ordem
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Tratamento Protetico - HTTP 201 (Tratamento Protetico Entrega Da Unitaria)
    [Documentation]    Impression, laboratory and delivery: the crown on 46 becomes Prosthesis in the odontogram.
    [Tags]    TratamentoProtetico    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRATAMENTO PROTETICO    201-entrega
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Tratamento Protetico - HTTP 422 (Tratamento Protetico Concluir Antes Da Entrega)
    [Documentation]    Concluding needs the delivery.
    [Tags]    TratamentoProtetico    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRATAMENTO PROTETICO    422-concluir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Tratamento Protetico - HTTP 400 (Tratamento Protetico Cancelar Sem Motivo)
    [Documentation]    Cancelling needs the reason.
    [Tags]    TratamentoProtetico    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    TRATAMENTO PROTETICO    400-cancelar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
