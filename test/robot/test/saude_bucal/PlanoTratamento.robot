*** Settings ***
Resource    ../../src/scenario/saude_bucal/plano_tratamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Plano Tratamento
Metadata    Test Suite Description        This test suite validates the dental treatment plan (ADR-0137) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PlanoTratamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-21
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Plano Tratamento - HTTP 201 (Plano Tratamento Abrir)
    [Documentation]    Opens a plan with two items, both planned.
    [Tags]    PlanoTratamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    201-abrir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Plano Tratamento - HTTP 409 (Plano Tratamento Segundo Plano Aberto)
    [Documentation]    One open plan per patient.
    [Tags]    PlanoTratamento    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    409-aberto
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Plano Tratamento - HTTP 422 (Plano Tratamento Fora Do Atendimento Odontologico)
    [Documentation]    The plan is opened in a dental-care visit.
    [Tags]    PlanoTratamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    422-atendimento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Plano Tratamento - HTTP 201 (Plano Tratamento Executar Pelo Procedimento)
    [Documentation]    The realized procedure concludes the item.
    [Tags]    PlanoTratamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    201-executar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Plano Tratamento - HTTP 422 (Plano Tratamento Procedimento Diferente Do Item)
    [Documentation]    The procedure must match the item tooth.
    [Tags]    PlanoTratamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    422-item
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Plano Tratamento - HTTP 422 (Plano Tratamento Concluir Com Pendente)
    [Documentation]    A planned item blocks the conclusion.
    [Tags]    PlanoTratamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    422-concluir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Plano Tratamento - HTTP 200 (Plano Tratamento Concluir)
    [Documentation]    With the item executed, the treatment is concluded.
    [Tags]    PlanoTratamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    200-concluir
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Plano Tratamento - HTTP 400 (Plano Tratamento Abandono Sem Motivo)
    [Documentation]    Abandoning needs the reason.
    [Tags]    PlanoTratamento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    400-abandono
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Plano Tratamento - HTTP 200 (Plano Tratamento Cancelar Item)
    [Documentation]    The dentist cancels an item with the reason.
    [Tags]    PlanoTratamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PLANO TRATAMENTO    200-cancelar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
